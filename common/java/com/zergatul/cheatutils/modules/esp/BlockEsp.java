package com.zergatul.cheatutils.modules.esp;

import com.google.common.base.Stopwatch;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.scripting.modules.BlockEspEvent;
import com.zergatul.cheatutils.scripting.types.BlockPosWrapper;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BlockEsp {

    public static final BlockEsp instance = new BlockEsp();

    private final List<CustomBlockPosEntry> customEntries = new ArrayList<>();
    private final List<BlockPos> bbList = new ArrayList<>();
    private final List<BlockPos> tracerList = new ArrayList<>();
    private final List<BlockPos> overlayList = new ArrayList<>();
    private boolean enabled = true;

    private BlockEsp() {
        Events.AfterRenderWorld.add(this::render);
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

    private void render(RenderWorldLastEvent event) {
        if (!enabled || !EspGlobal.enabled) {
            return;
        }

        ProfilerFiller profiler = Profiler.get();
        profiler.push(Constants.MOD_ID + " : BlockEspRender");
        var stopwatch = Stopwatch.createStarted();

        lastFrameGpuTime = 0;

        renderCustomEntries(event);
        renderConfiguredEntries(event);

        lastFrameRender = stopwatch.elapsed(TimeUnit.MICROSECONDS) / 1000f;
        profiler.pop();
    }

    public double lastFrameRender;
    public double lastFrameGpuTime;

    private void renderConfiguredEntries(RenderWorldLastEvent event) {
        ImmutableList<BlockEspConfig> configs = ConfigStore.instance.getConfig().blocks.getBlockConfigs();
        if (configs.isEmpty()) {
            return;
        }

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        LineRenderer lineRenderer = LineRenderer.getInstance();
        lineRenderer.begin();
        EspCubeLineRender cubeRenderer = EspCubeLineRender.getInstance();
        cubeRenderer.begin();

        for (BlockEspConfig config : configs) {
            if (!config.enabled) {
                continue;
            }

            Set<BlockPos> set = BlockFinder.instance.blocks.get(config);
            if (set == null || set.isEmpty()) {
                continue;
            }

            double tracerMaxDistanceSqr = config.getTracerMaxDistanceSqr();
            double boundingBoxMaxDistanceSqr = config.getBoundingBoxMaxDistanceSqr();
            double overlayMaxDistanceSqr = config.getOverlayMaxDistanceSqr();

            bbList.clear();
            tracerList.clear();
            overlayList.clear();

            if (config.scriptEnabled && config.script != null) {
                BlockScriptResult result = new BlockScriptResult();
                BlockEspEvent blockEspEvent = new BlockEspEvent(result);
                for (BlockPos pos : set) {
                    double dx = pos.getX() - playerX;
                    double dy = pos.getY() - playerY;
                    double dz = pos.getZ() - playerZ;
                    double distanceSqr = dx * dx + dy * dy + dz * dz;

                    if (distanceSqr >= boundingBoxMaxDistanceSqr && distanceSqr >= tracerMaxDistanceSqr && distanceSqr >= overlayMaxDistanceSqr) {
                        continue;
                    }

                    result.reset();
                    config.script.accept(new BlockPosWrapper(pos), blockEspEvent);

                    if (distanceSqr < boundingBoxMaxDistanceSqr && result.shouldDrawOutline(config.drawBoundingBox)) {
                        bbList.add(pos);
                    }

                    if (distanceSqr < tracerMaxDistanceSqr && result.shouldDrawTracer(config.drawTracers)) {
                        tracerList.add(pos);
                    }

                    if (distanceSqr < overlayMaxDistanceSqr && result.shouldDrawOverlay(config.drawOverlay)) {
                        overlayList.add(pos);
                    }
                }
            } else {
                for (BlockPos pos : set) {
                    double dx = pos.getX() - playerX;
                    double dy = pos.getY() - playerY;
                    double dz = pos.getZ() - playerZ;
                    double distanceSqr = dx * dx + dy * dy + dz * dz;

                    if (config.drawBoundingBox && distanceSqr < boundingBoxMaxDistanceSqr) {
                        bbList.add(pos);
                    }

                    if (config.drawTracers && distanceSqr < tracerMaxDistanceSqr) {
                        tracerList.add(pos);
                    }

                    if (config.drawOverlay && distanceSqr < overlayMaxDistanceSqr) {
                        overlayList.add(pos);
                    }
                }
            }

            if (!bbList.isEmpty()) {
                renderBoundingBoxes(cubeRenderer, (float) config.boundingBoxWidth, config.boundingBoxColor.getRGB(), event);
            }

            if (!tracerList.isEmpty()) {
                renderTracers(lineRenderer, (float) config.tracerWidth, config.tracerColor.getRGB(), event);
            }

            if (!overlayList.isEmpty()) {
                renderOverlay(config.overlayColor, event);
            }
        }

        var sw = Stopwatch.createStarted();
        lineRenderer.end(event.getMvp());
        cubeRenderer.end(event.getMvp());
        lastFrameGpuTime += sw.elapsed(TimeUnit.MICROSECONDS) / 1000d;
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

    private void renderTracers(LineRenderer renderer, float width, int color, RenderWorldLastEvent event) {
        Vec3 cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        Vec3 tracerCenter = event.getTracerCenter();
        float tracerX = (float) (tracerCenter.x - cameraX);
        float tracerY = (float) (tracerCenter.y - cameraY);
        float tracerZ = (float) (tracerCenter.z - cameraZ);

        for (BlockPos pos : tracerList) {
            renderer.line(
                    tracerX, tracerY, tracerZ,
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
        var sw = Stopwatch.createStarted();
        renderer.end(event.getMvp(), color);
        lastFrameGpuTime += sw.elapsed(TimeUnit.MICROSECONDS) / 1000d;
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