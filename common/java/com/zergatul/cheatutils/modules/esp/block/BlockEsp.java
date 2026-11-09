package com.zergatul.cheatutils.modules.esp.block;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.BlockEspConfig;
import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import com.zergatul.cheatutils.modules.esp.EspGlobal;
import com.zergatul.cheatutils.modules.esp.block.web.*;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.scripting.events.BlockEspConsumer;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.modules.BlockEspEvent;
import com.zergatul.cheatutils.scripting.types.BlockPosWrapper;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BlockEsp {

    public static final BlockEsp INSTANCE = new BlockEsp();

    private final SettingHandle2<BlockEspConfig> settings = Settings.get(BlockEspConfig.class);
    private final List<CustomBlockPosEntry> customEntries = new ArrayList<>();
    private final List<BlockPos> bbList = new ArrayList<>();
    private final List<BlockPos> tracerList = new ArrayList<>();
    private final List<BlockPos> overlayList = new ArrayList<>();
    private final Map<Block, ScriptActivation<BlockEspConsumer>> scripts = new IdentityHashMap<>();

    private BlockEspConfig currentConfig;
    private boolean enabled = true;

    private BlockEsp() {
        Events.AfterRenderWorld.add(this::render);
        settings.onChanged(this::onConfigChanged);

        WebApiRegistry.INSTANCE.register(new ConfigWebApi());
        WebApiRegistry.INSTANCE.register(new RescanWebApi());
        WebApiRegistry.INSTANCE.register(new BlockInfoWebApi());
        WebApiRegistry.INSTANCE.register(new BlockModelWebApi());
        WebApiRegistry.INSTANCE.register(new CodeWebApi());

        currentConfig = settings.get();
        BlockFinder.INSTANCE.trackBlocks(new HashSet<>(currentConfig.entries().stream().flatMap(e -> e.blocks().stream()).toList()));
    }

    public SettingHandle2<BlockEspConfig> getSettings() {
        return settings;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void addCustom(BlockPos pos, int color) {
        customEntries.removeIf(e -> e.pos.equals(pos));
        customEntries.add(new CustomBlockPosEntry(pos.immutable(), color));
    }

    public void clearCustom() {
        customEntries.clear();
    }

    public void removeCustom(BlockPos pos) {
        customEntries.removeIf(e -> e.pos.equals(pos));
    }

    public void setScript(BlockEspEntry entry, @Nullable BlockEspConsumer script) {
        // Have to run from the main thread, since BlockEsp module doesn't snapshot scripts
        // and if update happens mid-frame it can cause NullReference exception.
        ClientTickEndExecutor.instance.execute(() -> {
            List<ScriptActivation<BlockEspConsumer>> previous = new ArrayList<>();
            for (Block block : entry.blocks()) {
                ScriptActivation<BlockEspConsumer> activation = scripts.remove(block);
                if (activation != null && !previous.contains(activation)) {
                    previous.add(activation);
                    activation.deactivate();
                }
            }
            if (script != null) {
                String identifier = new BlockGroupKey(entry.blocks()).toString();
                ScriptActivation<BlockEspConsumer> activation = new ScriptActivation<>(new ScriptRef(ScriptType.BLOCK_ESP, identifier), script);
                for (Block block : entry.blocks()) {
                    scripts.put(block, activation);
                }
            }
        });
    }

    public void clearScripts() {
        ClientTickEndExecutor.instance.execute(() -> {
            scripts.values().forEach(ScriptActivation::deactivate);
            scripts.clear();
        });
    }

    public void moveScript(ImmutableList<Block> oldBlocks, ImmutableList<Block> newBlocks, ScriptRef newRef) {
        ClientTickEndExecutor.instance.execute(() -> {
            // all blocks should point to the same ScriptActivation instance
            ScriptActivation<BlockEspConsumer> activation = scripts.get(oldBlocks.get(0));
            for (Block block : oldBlocks) {
                scripts.remove(block);
            }
            if (activation != null) {
                activation = activation.withRef(newRef);
                for (Block block : newBlocks) {
                    scripts.put(block, activation);
                }
            }
        });
    }

    private void onConfigChanged(BlockEspConfig prev, BlockEspConfig curr) {
        Set<Block> blocks = new HashSet<>();
        for (BlockEspEntry config : curr.entries()) {
            for (Block block : config.blocks()) {
                blocks.add(block);
            }
        }

        BlockFinder.INSTANCE.trackBlocks(blocks);

        ClientTickEndExecutor.instance.execute(() -> {
            this.currentConfig = curr;
        });
    }

    private void render(RenderWorldLastEvent event) {
        if (!enabled || !EspGlobal.enabled) {
            return;
        }

        ProfilerFiller profiler = Profiler.get();
        profiler.push(Constants.MOD_ID + " : BlockEspRender");

        renderCustomEntries(event);
        renderConfiguredEntries(event);

        profiler.pop();
    }

    private void renderConfiguredEntries(RenderWorldLastEvent event) {
        if (currentConfig.entries().isEmpty()) {
            return;
        }

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        TracerRenderer tracerRenderer = TracerRenderer.getInstance();
        tracerRenderer.begin();
        EspCubeLineRender cubeRenderer = EspCubeLineRender.getInstance();
        cubeRenderer.begin();

        for (BlockEspEntry config : currentConfig.entries()) {
            if (!config.enabled()) {
                continue;
            }

            double tracerMaxDistanceSqr = config.getTracerMaxDistanceSqr();
            double boundingBoxMaxDistanceSqr = config.getBoundingBoxMaxDistanceSqr();
            double overlayMaxDistanceSqr = config.getOverlayMaxDistanceSqr();

            bbList.clear();
            tracerList.clear();
            overlayList.clear();

            for (Block block : config.blocks()) {
                ScriptActivation<BlockEspConsumer> script = scripts.get(block);
                if (config.scriptEnabled() && script != null && script.isActive()) {
                    BlockScriptResult result = new BlockScriptResult();
                    BlockEspEvent blockEspEvent = new BlockEspEvent(result);
                    Set<BlockPos> positions = BlockFinder.INSTANCE.getPositons(block);
                    if (positions == null) {
                        continue;
                    }
                    for (BlockPos pos : positions) {
                        double dx = pos.getX() - playerX;
                        double dy = pos.getY() - playerY;
                        double dz = pos.getZ() - playerZ;
                        double distanceSqr = dx * dx + dy * dy + dz * dz;

                        if (distanceSqr >= boundingBoxMaxDistanceSqr && distanceSqr >= tracerMaxDistanceSqr && distanceSqr >= overlayMaxDistanceSqr) {
                            continue;
                        }

                        result.reset();
                        if (!script.run("block rendering", () -> script.program.accept(new BlockPosWrapper(pos), blockEspEvent))) {
                            // Discard partial scripted output; normal configured visuals resume next frame.
                            bbList.clear();
                            tracerList.clear();
                            overlayList.clear();
                            break;
                        }

                        if (distanceSqr < boundingBoxMaxDistanceSqr && result.shouldDrawOutline(config.drawBoundingBox())) {
                            bbList.add(pos);
                        }

                        if (distanceSqr < tracerMaxDistanceSqr && result.shouldDrawTracer(config.drawTracers())) {
                            tracerList.add(pos);
                        }

                        if (distanceSqr < overlayMaxDistanceSqr && result.shouldDrawOverlay(config.drawOverlay())) {
                            overlayList.add(pos);
                        }
                    }
                } else {
                    Set<BlockPos> positions = BlockFinder.INSTANCE.getPositons(block);
                    if (positions == null) {
                        continue;
                    }
                    for (BlockPos pos : positions) {
                        double dx = pos.getX() - playerX;
                        double dy = pos.getY() - playerY;
                        double dz = pos.getZ() - playerZ;
                        double distanceSqr = dx * dx + dy * dy + dz * dz;

                        if (config.drawBoundingBox() && distanceSqr < boundingBoxMaxDistanceSqr) {
                            bbList.add(pos);
                        }

                        if (config.drawTracers() && distanceSqr < tracerMaxDistanceSqr) {
                            tracerList.add(pos);
                        }

                        if (config.drawOverlay() && distanceSqr < overlayMaxDistanceSqr) {
                            overlayList.add(pos);
                        }
                    }
                }
            }

            if (!bbList.isEmpty()) {
                renderBoundingBoxes(cubeRenderer, (float) config.boundingBoxWidth(), config.boundingBoxColor().getRGB(), event);
            }

            if (!tracerList.isEmpty()) {
                renderTracers(tracerRenderer, (float) config.tracerWidth(), config.tracerColor().getRGB(), event);
            }

            if (!overlayList.isEmpty()) {
                renderOverlay(config.overlayColor(), event);
            }
        }

        tracerRenderer.end(event.getMvp());
        cubeRenderer.end(event.getMvp());
    }

    private void renderBoundingBoxes(EspCubeLineRender renderer, float width, int color, RenderWorldLastEvent event) {
        Vec3 cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        for (BlockPos pos : bbList) {
            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();
            renderer.cube(
                    (float) (x - cameraX),
                    (float) (y - cameraY),
                    (float) (z - cameraZ),
                    color,
                    width);
        }
    }

    private void renderTracers(TracerRenderer renderer, float width, int color, RenderWorldLastEvent event) {
        Vec3 cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        for (BlockPos pos : tracerList) {
            renderer.tracer(
                    (float) (pos.getX() + 0.5 - cameraX),
                    (float) (pos.getY() + 0.5 - cameraY),
                    (float) (pos.getZ() + 0.5 - cameraZ),
                    color, width);
        }
    }

    private void renderOverlay(Color color, RenderWorldLastEvent event) {
        Vec3 cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        BlockEspOverlayRenderer renderer = BlockEspOverlayRenderer.getInstance();
        renderer.begin();
        for (BlockPos pos : overlayList) {
            renderer.submitBlock(
                    (float) (pos.getX() - cameraX),
                    (float) (pos.getY() - cameraY),
                    (float) (pos.getZ() - cameraZ));
        }
        renderer.end(event.getMvp(), color);
    }

    private void renderCustomEntries(RenderWorldLastEvent event) {
        if (!customEntries.isEmpty()) {
            final float shift = 0.01f;
            Vec3 cameraPos = event.getCameraPos();
            Position3dColorRenderer renderer = Position3dColorRenderer.getInstance();
            renderer.begin();
            for (CustomBlockPosEntry entry : customEntries) {
                renderer.cuboid(
                        (float) (entry.pos.getX() - cameraPos.x - shift),
                        (float) (entry.pos.getY() - cameraPos.y - shift),
                        (float) (entry.pos.getZ() - cameraPos.z - shift),
                        (float) (entry.pos.getX() - cameraPos.x + 1 + shift),
                        (float) (entry.pos.getY() - cameraPos.y + 1 + shift),
                        (float) (entry.pos.getZ() - cameraPos.z + 1 + shift),
                        entry.color);
            }
            renderer.end(event.getMvp());
        }
    }

    public static class BlockScriptResult {

        public int tracer;
        public int outline;
        public int overlay;

        public void reset() {
            tracer = -1;
            outline = -1;
            overlay = -1;
        }

        public boolean shouldDrawTracer(boolean setting) {
            if (tracer == -1) {
                return setting;
            }
            return tracer != 0;
        }

        public boolean shouldDrawOutline(boolean setting) {
            if (outline == -1) {
                return setting;
            }
            return outline != 0;
        }

        public boolean shouldDrawOverlay(boolean setting) {
            if (overlay == -1) {
                return setting;
            }
            return overlay != 0;
        }
    }

    private record CustomBlockPosEntry(BlockPos pos, int color) {}
}