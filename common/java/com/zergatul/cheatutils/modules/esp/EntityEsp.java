package com.zergatul.cheatutils.modules.esp;

import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.scripting.modules.EntityEspEvent;
import com.zergatul.cheatutils.utils.ColorUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class EntityEsp implements Module {

    public static final EntityEsp instance = new EntityEsp();

    private final Minecraft mc = Minecraft.getInstance();
    private final Map<EntityEspConfig, List<EntityRenderState>> overlayEntityStates = new IdentityHashMap<>();
    private final Map<EntityEspConfig, List<EntityRenderState>> outlineEntityStates = new IdentityHashMap<>();
    private final EntityEspOverlayRenderer overlayRenderer = new EntityEspOverlayRenderer();
    private final EntityEspOutlineRenderer outlineRenderer = new EntityEspOutlineRenderer();
    private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
    private final OutlineCaptureBufferSource bufferSource = new OutlineCaptureBufferSource();
    private final FeatureRenderDispatcher dispatcher;
    private final Map<EntityScriptResultKey, EntityScriptResult> scriptResults = new HashMap<>();
    private boolean enabled = true;

    private EntityEsp() {
        Events.BeforeRenderWorld.add(this::onBeforeRenderWorld);
        Events.AfterRenderWorld.add(this::onAfterRenderWorld);

        this.dispatcher = new FeatureRenderDispatcher(
                submitNodeStorage,
                mc.getModelManager(),
                bufferSource,
                mc.getAtlasManager(),
                new OutlineBufferSource(),
                new EmptyBufferSource(),
                mc.font,
                mc.gameRenderer.getGameRenderState());
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
                        entity.distanceToSqr(mc.player) < config.getGlowMaxDistanceSqr() &&
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

    public boolean shouldEntityGlow(Entity entity) {
        if (!enabled || !EspGlobal.enabled) {
            return false;
        }
        if (mc.player == null) {
            return false;
        }
        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity) && entity.distanceToSqr(mc.player) < config.getGlowMaxDistanceSqr()) {
                return !isOutlineDisabledFromScript(config, entity);
            }
        }
        return false;
    }

    public Integer getGlowColor(Entity entity) {
        if (!EspGlobal.enabled) {
            return null;
        }
        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity)) {
                return config.glowColor.getRGB();
            }
        }
        return null;
    }

    public StylizedText getTitleOverride(EntityEspConfig config, Entity entity) {
        if (!config.scriptEnabled || config.script == null) {
            return null;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return result.title;
        }

        return executeScript(config, entity).title;
    }

    private void onBeforeRenderWorld() {
        scriptResults.clear();
    }

    private void onAfterRenderWorld(RenderWorldLastEvent event) {
        assert mc.level != null && mc.player != null;

        if (!enabled || !EspGlobal.enabled) {
            return;
        }

        float partialTicks = event.getPartialTickTime();

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        Vec3 tracerCenter = event.getTracerCenter();
        double tracerX = tracerCenter.x;
        double tracerY = tracerCenter.y;
        double tracerZ = tracerCenter.z;

        MainFrameBuffer.bind();

        LineRenderer lineRenderer = RenderUtilities.instance.getLineRenderer();
        ThickLineRenderer thickLineRenderer = RenderUtilities.instance.getThickLineRenderer();

        lineRenderer.begin(event, false);
        thickLineRenderer.begin(event, false);

        ImmutableList<EntityEspConfig> list = ConfigStore.instance.getConfig().entities.configs;
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

            EntityEspConfig config = list.stream().filter(c ->
                    c.enabled &&
                    c.drawOutline &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getOutlineMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null && !isCollisionBoxDisabledFromScript(config, entity)) {
                Vec3 pos = entity.getPosition(partialTicks);
                AABB box = entity.getDimensions(entity.getPose()).makeBoundingBox(pos);

                float r = config.outlineColor.getRed() / 255f;
                float g = config.outlineColor.getGreen() / 255f;
                float b = config.outlineColor.getBlue() / 255f;
                float a = config.outlineColor.getAlpha() / 255f;

                final int lineWidth = config.outlineWidth;
                if (lineWidth == 1) {
                    lineRenderer.cuboid(
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            r, g, b, a);
                } else {
                    thickLineRenderer.setWidth(lineWidth);
                    thickLineRenderer.cuboid(
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            r, g, b, a);
                }
            }

            config = list.stream().filter(c ->
                    c.enabled &&
                            c.drawTracers &&
                            c.clazz.isInstance(entity) &&
                            distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null && !isTracerDisabledFromScript(config, entity)) {
                Integer colorOverrideBoxed = getTracerColorOverride(config, entity);
                float r, g, b, a;
                if (colorOverrideBoxed == null) {
                    r = config.tracerColor.getRed() / 255f;
                    g = config.tracerColor.getGreen() / 255f;
                    b = config.tracerColor.getBlue() / 255f;
                    a = config.tracerColor.getAlpha() / 255f;
                } else {
                    int color = colorOverrideBoxed;
                    r = ColorUtils.r(color);
                    g = ColorUtils.g(color);
                    b = ColorUtils.b(color);
                    a = ColorUtils.a(color);
                }

                Vec3 pos = entity.getPosition(event.getPartialTickTime());
                final int lineWidth = config.tracerWidth;
                if (lineWidth == 1) {
                    lineRenderer.line(tracerX, tracerY, tracerZ, pos.x, pos.y, pos.z, r, g, b, a);
                } else {
                    thickLineRenderer.setWidth(lineWidth);
                    thickLineRenderer.line(tracerX, tracerY, tracerZ, pos.x, pos.y, pos.z, r, g, b, a);
                }
            }
        }

        lineRenderer.end();
        thickLineRenderer.end();

        drawOverlays(list, event);
        drawOutlines(list, event);

        overlayEntityStates.clear();
        outlineEntityStates.clear();
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
        for (EntityEspConfig config : list) {
            List<EntityRenderState> states = overlayEntityStates.get(config);
            if (states == null || states.isEmpty()) {
                continue;
            }

            overlayRenderer.begin();

            for (EntityRenderState state : states) {
                int outlineColor = state.outlineColor;
                state.outlineColor = 0;
                renderDispatcher.submit(state, event.getCameraRenderState(), state.x - camX, state.y - camY, state.z - camZ, poseStack, submitNodeStorage);
                state.outlineColor = outlineColor;
            }

            dispatcher.renderAllFeatures();
            bufferSource.endBatch();
            overlayRenderer.end(config.overlayColor);
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
        for (EntityEspConfig config : list) {
            List<EntityRenderState> states = outlineEntityStates.get(config);
            if (states == null || states.isEmpty()) {
                continue;
            }

            outlineRenderer.begin();

            for (EntityRenderState state : states) {
                int outlineColor = state.outlineColor;
                state.outlineColor = 0;
                renderDispatcher.submit(state, event.getCameraRenderState(), state.x - camX, state.y - camY, state.z - camZ, poseStack, submitNodeStorage);
                state.outlineColor = outlineColor;
            }

            dispatcher.renderAllFeatures();
            bufferSource.endBatch();
            outlineRenderer.end(config.glowColor);
        }
    }

    private static final class OutlineCaptureBufferSource extends MultiBufferSource.BufferSource {

        public OutlineCaptureBufferSource() {
            super(new ByteBufferBuilder(1 << 16), Object2ObjectSortedMaps.emptyMap());
        }

        @Override
        public @NonNull VertexConsumer getBuffer(final RenderType renderType) {
            if (renderType.isOutline()) {
                return super.getBuffer(renderType);
            }

            return renderType.outline().map(super::getBuffer).orElse(EmptyVertexConsumer.instance);
        }
    }

    private static final class EmptyBufferSource extends MultiBufferSource.BufferSource {
        public EmptyBufferSource() {
            super(ByteBufferBuilder.exactlySized(4), Object2ObjectSortedMaps.emptyMap());
        }
    }

    private boolean isCollisionBoxDisabledFromScript(EntityEspConfig config, Entity entity) {
        return getBooleanFromScript(config, entity, result -> result.collisionBoxDisabled);
    }

    private boolean isTracerDisabledFromScript(EntityEspConfig config, Entity entity) {
        return getBooleanFromScript(config, entity, result -> result.tracerDisabled);
    }

    private boolean isOverlayDisabledFromScript(EntityEspConfig config, Entity entity) {
        return getBooleanFromScript(config, entity, result -> result.overlayDisabled);
    }

    private boolean isOutlineDisabledFromScript(EntityEspConfig config, Entity entity) {
        return getBooleanFromScript(config, entity, result -> result.outlineDisabled);
    }

    private Integer getTracerColorOverride(EntityEspConfig config, Entity entity) {
        if (!config.scriptEnabled || config.script == null) {
            return null;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return result.tracerColorOverride;
        } else {
            return executeScript(config, entity).tracerColorOverride;
        }
    }

    private boolean getBooleanFromScript(EntityEspConfig config, Entity entity, Predicate<EntityScriptResult> predicate) {
        if (!config.scriptEnabled || config.script == null) {
            return false;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return predicate.test(result);
        }

        return predicate.test(executeScript(config, entity));
    }

    private EntityScriptResult executeScript(EntityEspConfig config, Entity entity) {
        assert config.script != null;

        EntityScriptResult result = new EntityScriptResult(entity.getId(), config);
        scriptResults.put(new EntityScriptResultKey(entity.getId(), config), result);
        config.script.accept(entity.getId(), new EntityEspEvent(result));
        return result;
    }

    public static class EntityScriptResult {

        public final int id;
        public final EntityEspConfig config;
        public boolean tracerDisabled;
        public boolean outlineDisabled;
        public boolean overlayDisabled;
        public boolean collisionBoxDisabled;
        public StylizedText title;
        public Integer tracerColorOverride;

        public EntityScriptResult(int id, EntityEspConfig config) {
            this.id = id;
            this.config = config;
        }
    }

    private static class EntityScriptResultKey {

        private final int id;
        private final EntityEspConfig config;

        public EntityScriptResultKey(int id, EntityEspConfig config) {
            this.id = id;
            this.config = config;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EntityScriptResultKey other) {
                return other.id == id && other.config == config;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * id + config.hashCode();
        }
    }
}