package com.zergatul.cheatutils.modules.esp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.esp.entity.EntityEspScriptRuntime;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.List;

public class EntityEsp implements Module {

    public static final EntityEsp instance = new EntityEsp();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<MatchedEntity> bbList = new ArrayList<>();
    private final List<MatchedEntity> tracerList = new ArrayList<>();
    private final Map<EntityEspConfig, List<EntityRenderState>> overlayEntityStates = new IdentityHashMap<>();
    private final Map<EntityEspConfig, List<EntityRenderState>> outlineEntityStates = new IdentityHashMap<>();
    private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
    private final RenderBuffers renderBuffers = new RenderBuffers(1);
    private final FeatureRenderDispatcher dispatcher;
    private final EntityEspScriptRuntime scriptRuntime;
    private boolean enabled = true;

    private EntityEsp() {
        Events.BeforeRenderWorld.add(this::onBeforeRenderWorld);
        Events.AfterRenderWorld.add(this::onAfterRenderWorld);
        Events.RenderBuffersCleanUp.add(this::onRenderBuffersCleanUp);
        Events.Close.add(this::onClose);

        this.dispatcher = new FeatureRenderDispatcher(
                renderBuffers,
                mc.getModelManager(),
                mc.getAtlasManager(),
                mc.font,
                mc.gameRenderer.gameRenderState());

        this.scriptRuntime = EntityEspScriptRuntime.INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void captureEntityRenderState(Entity entity, EntityRenderState renderState) {
        if (mc.player == null || !EspGlobal.enabled || !enabled) {
            return;
        }

        boolean overlayFound = false;
        boolean outlineFound = false;

        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (!config.enabled) {
                continue;
            }

            if (!config.isValidEntity(entity)) {
                continue;
            }

            if (!overlayFound) {
                overlayFound = config.drawOverlay &&
                        entity.distanceToSqr(mc.player) < config.getOverlayMaxDistanceSqr() &&
                        !isOverlayDisabledFromScript(config, entity);
                if (overlayFound) {
                    List<EntityRenderState> states = overlayEntityStates.computeIfAbsent(config, _ -> new ArrayList<>());
                    states.add(renderState);
                    if (outlineFound) {
                        break;
                    }
                }
            }

            if (!outlineFound) {
                outlineFound = config.useModOutline() &&
                        entity.distanceToSqr(mc.player) < config.getOutlineMaxDistanceSqr() &&
                        !isOutlineDisabledFromScript(config, entity);
                if (outlineFound) {
                    List<EntityRenderState> states = outlineEntityStates.computeIfAbsent(config, _ -> new ArrayList<>());
                    states.add(renderState);
                    if (overlayFound) {
                        break;
                    }
                }
            }
        }
    }

    public boolean shouldEntityHaveOutline(Entity entity) {
        if (!enabled || !EspGlobal.enabled) {
            return false;
        }
        if (mc.player == null) {
            return false;
        }
        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity) && entity.distanceToSqr(mc.player) < config.getOutlineMaxDistanceSqr()) {
                return !isOutlineDisabledFromScript(config, entity);
            }
        }
        return false;
    }

    public Integer getOutlineColor(Entity entity) {
        if (!EspGlobal.enabled) {
            return null;
        }
        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity)) {
                return config.outlineColor.getRGB();
            }
        }
        return null;
    }

    private void onBeforeRenderWorld() {
        scriptRuntime.clearScriptResults();
    }

    private void onAfterRenderWorld(RenderWorldLastEvent event) {
        assert mc.level != null && mc.player != null;

        if (!enabled || !EspGlobal.enabled) {
            return;
        }

        ImmutableList<EntityEspConfig> list = ConfigStore.instance.getConfig().entities.configs;
        if (list.isEmpty()) {
            return;
        }

        ProfilerFiller profiler = Profiler.get();
        profiler.push(Constants.MOD_ID + " : EntityEspRender");

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        LineRenderer lineRenderer = LineRenderer.getInstance();
        lineRenderer.begin();
        TracerRenderer tracerRenderer = TracerRenderer.getInstance();
        tracerRenderer.begin();

        bbList.clear();
        tracerList.clear();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LocalPlayer) {
                continue;
            }

            if (entity.isRemoved()) {
                continue;
            }

            double dx = entity.getX() - playerX;
            double dy = entity.getY() - playerY;
            double dz = entity.getZ() - playerZ;
            double distanceSqr = dx * dx + dy * dy + dz * dz;

            EntityEspConfig bbConfig = list.stream().filter(c ->
                    c.enabled &&
                    c.drawBoundingBox &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getBoundingBoxMaxDistanceSqr()).findFirst().orElse(null);

            if (bbConfig != null && !isCollisionBoxDisabledFromScript(bbConfig, entity)) {
                bbList.add(new MatchedEntity(entity, bbConfig));
            }

            EntityEspConfig tracerConfig = list.stream().filter(c ->
                    c.enabled &&
                            c.drawTracers &&
                            c.clazz.isInstance(entity) &&
                            distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (tracerConfig != null && !isTracerDisabledFromScript(tracerConfig, entity)) {
                tracerList.add(new MatchedEntity(entity, tracerConfig, scriptRuntime.getTracerColorOverride(tracerConfig, entity)));
            }
        }

        if (!bbList.isEmpty() || !tracerList.isEmpty()) {
            renderLines(lineRenderer, tracerRenderer, event);
        }

        lineRenderer.end(event.getMvp());
        tracerRenderer.end(event.getMvp());

        drawOverlays(list, event);
        drawOutlines(list, event);

        overlayEntityStates.clear();
        outlineEntityStates.clear();

        profiler.pop();
    }

    private void onRenderBuffersCleanUp() {
        this.renderBuffers.endFrame();
    }

    private void onClose() {
        dispatcher.close();
        renderBuffers.close();
    }

    private void renderLines(LineRenderer lineRenderer, TracerRenderer tracerRenderer, RenderWorldLastEvent event) {
        Vec3 cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        float partialTicks = event.getPartialTickTime();

        for (MatchedEntity entry : bbList) {
            Vec3 pos = entry.entity.getPosition(partialTicks);
            AABB box = entry.entity.getDimensions(entry.entity.getPose()).makeBoundingBox(pos);
            lineRenderer.cuboid(
                    (float) (box.minX - cameraX), (float) (box.minY - cameraY), (float) (box.minZ - cameraZ),
                    (float) (box.maxX - cameraX), (float) (box.maxY - cameraY), (float) (box.maxZ - cameraZ),
                    entry.config.boundingBoxColor.getRGB(),
                    (float) entry.config.boundingBoxWidth);
        }

        for (MatchedEntity entry : tracerList) {
            Vec3 pos = entry.entity.getPosition(partialTicks);
            tracerRenderer.tracer(
                    (float) (pos.x - cameraX), (float) (pos.y - cameraY), (float) (pos.z - cameraZ),
                    entry.colorOverride != null ? entry.colorOverride : entry.config.tracerColor.getRGB(),
                    (float) entry.config.tracerWidth);
        }
    }

    private void drawOverlays(ImmutableList<EntityEspConfig> list, RenderWorldLastEvent event) {
        if (overlayEntityStates.isEmpty()) {
            return;
        }

        Vec3 cameraPos = event.getCameraPos();
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        PoseStack poseStack = new PoseStack();
        EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
        EntityEspOverlayRenderer renderer = EntityEspOverlayRenderer.getInstance();
        for (EntityEspConfig config : list) {
            List<EntityRenderState> states = overlayEntityStates.get(config);
            if (states == null || states.isEmpty()) {
                continue;
            }

            renderer.begin();
            submitEntityMasks(states, event, renderDispatcher, poseStack, camX, camY, camZ);
            drawSubmittedMasks();
            renderer.end(config.overlayColor);
        }
    }

    private void drawOutlines(ImmutableList<EntityEspConfig> list, RenderWorldLastEvent event) {
        if (outlineEntityStates.isEmpty()) {
            return;
        }

        Vec3 cameraPos = event.getCameraPos();
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        PoseStack poseStack = new PoseStack();
        EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
        EntityEspOutlineRenderer renderer = EntityEspOutlineRenderer.getInstance();
        for (EntityEspConfig config : list) {
            List<EntityRenderState> states = outlineEntityStates.get(config);
            if (states == null || states.isEmpty()) {
                continue;
            }

            renderer.begin();
            submitEntityMasks(states, event, renderDispatcher, poseStack, camX, camY, camZ);
            drawSubmittedMasks();
            renderer.end(config.outlineColor);
        }
    }

    private void submitEntityMasks(
            List<EntityRenderState> states,
            RenderWorldLastEvent event,
            EntityRenderDispatcher renderDispatcher,
            PoseStack poseStack,
            double camX,
            double camY,
            double camZ
    ) {
        for (EntityRenderState state : states) {
            int outlineColor = state.outlineColor;
            state.outlineColor = -1;
            renderDispatcher.submit(state, event.getCameraRenderState(), state.x - camX, state.y - camY, state.z - camZ, poseStack, submitNodeStorage);
            state.outlineColor = outlineColor;
        }
    }

    private void drawSubmittedMasks() {
        try (FeatureRenderDispatcher.PreparedFrame frame = dispatcher.prepareFrame(submitNodeStorage)) {
            frame.executeOutline();
        }
    }

    private boolean isCollisionBoxDisabledFromScript(EntityEspConfig config, Entity entity) {
        return scriptRuntime.getBooleanFromScript(config, entity, result -> result.collisionBoxDisabled);
    }

    private boolean isTracerDisabledFromScript(EntityEspConfig config, Entity entity) {
        return scriptRuntime.getBooleanFromScript(config, entity, result -> result.tracerDisabled);
    }

    private boolean isOverlayDisabledFromScript(EntityEspConfig config, Entity entity) {
        return scriptRuntime.getBooleanFromScript(config, entity, result -> result.overlayDisabled);
    }

    private boolean isOutlineDisabledFromScript(EntityEspConfig config, Entity entity) {
        return scriptRuntime.getBooleanFromScript(config, entity, result -> result.outlineDisabled);
    }

    private record MatchedEntity(Entity entity, EntityEspConfig config, @Nullable Integer colorOverride) {
        public MatchedEntity(Entity entity, EntityEspConfig config) {
            this(entity, config, null);
        }
    }
}