package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.List;

public class EntityEsp implements Module {

    public static final EntityEsp INSTANCE = new EntityEsp();

    private final Minecraft mc = Minecraft.getMinecraft();
    private final List<MatchedEntity> bbList = new ArrayList<>();
    private final List<MatchedEntity> tracerList = new ArrayList<>();
    private final Map<EntityEspConfig, List<BufferedVerticesEntry>> overlayEntityStates = new IdentityHashMap<>();
    private final Map<EntityEspConfig, List<BufferedVerticesEntry>> outlineEntityStates = new IdentityHashMap<>();
    private boolean enabled = true;

    private EntityEsp() {
        Events.AfterRenderWorld.add(this::onAfterRenderWorld);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void captureEntityRenderState(Entity entity, BufferedVerticesEntry buffer) {
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
                overlayFound = config.drawOverlay && entity.getDistanceSq(mc.player) < config.getOverlayMaxDistanceSqr();
                if (overlayFound) {
                    List<BufferedVerticesEntry> buffers = overlayEntityStates.computeIfAbsent(config, unused -> new ArrayList<>());
                    buffers.add(buffer);
                    if (outlineFound) {
                        break;
                    }
                }
            }

            if (!outlineFound) {
                outlineFound = config.useModOutline() && entity.getDistanceSq(mc.player) < config.getOutlineMaxDistanceSqr();
                if (outlineFound) {
                    List<BufferedVerticesEntry> states = outlineEntityStates.computeIfAbsent(config, unused -> new ArrayList<>());
                    states.add(buffer);
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
            if (config.useMinecraftOutline() && config.isValidEntity(entity) && entity.getDistanceSq(mc.player) < config.getOutlineMaxDistanceSqr()) {
                return true;
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

    private void onAfterRenderWorld(RenderWorldLastEvent event) {
        assert mc.world != null && mc.player != null;

        if (!enabled || !EspGlobal.enabled) {
            return;
        }

        ImmutableList<EntityEspConfig> list = ConfigStore.instance.getConfig().entities.configs;
        if (list.isEmpty()) {
            return;
        }

        Vec3d playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        EspCuboidLineRenderer cuboidLineRenderer = EspCuboidLineRenderer.INSTANCE;
        cuboidLineRenderer.begin();
        TracerRenderer tracerRenderer = TracerRenderer.INSTANCE;
        tracerRenderer.begin();

        bbList.clear();
        tracerList.clear();

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityPlayerSP) {
                continue;
            }

            if (!entity.isAddedToWorld()) {
                continue;
            }

            double dx = entity.posX - playerX;
            double dy = entity.posY - playerY;
            double dz = entity.posZ - playerZ;
            double distanceSqr = dx * dx + dy * dy + dz * dz;

            EntityEspConfig bbConfig = list.stream().filter(c ->
                    c.enabled &&
                    c.drawBoundingBox &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getBoundingBoxMaxDistanceSqr()).findFirst().orElse(null);

            if (bbConfig != null) {
                bbList.add(new MatchedEntity(entity, bbConfig));
            }

            EntityEspConfig tracerConfig = list.stream().filter(c ->
                    c.enabled &&
                    c.drawTracers &&
                    c.clazz.isInstance(entity) &&
                    distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (tracerConfig != null) {
                tracerList.add(new MatchedEntity(entity, tracerConfig));
            }
        }

        if (!bbList.isEmpty() || !tracerList.isEmpty()) {
            renderLines(cuboidLineRenderer, tracerRenderer, event);
        }

        cuboidLineRenderer.end(event.getMvp());
        tracerRenderer.end(event.getMvp());

        drawOverlays(list, event);
        drawOutlines(list, event);

        overlayEntityStates.clear();
        outlineEntityStates.clear();
    }

    private void renderLines(EspCuboidLineRenderer lineRenderer, TracerRenderer tracerRenderer, RenderWorldLastEvent event) {
        Vec3d cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        float partialTicks = event.getPartialTicks();

        for (MatchedEntity entry : bbList) {
            Entity entity = entry.entity;
            double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
            double halfWidth = entity.width / 2;
            double height = entity.height;
            lineRenderer.cuboid(
                    (float) (posX - halfWidth - cameraX),
                    (float) (posY - cameraY),
                    (float) (posZ - halfWidth - cameraZ),
                    (float) (posX + halfWidth - cameraX),
                    (float) (posY + height - cameraY),
                    (float) (posZ + halfWidth - cameraZ),
                    entry.config.boundingBoxColor.getRGB(),
                    (float) entry.config.boundingBoxWidth);
        }

        for (MatchedEntity entry : tracerList) {
            Entity entity = entry.entity;
            double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
            tracerRenderer.tracer(
                    (float) (posX - cameraX), (float) (posY - cameraY), (float) (posZ - cameraZ),
                    entry.config.tracerColor.getRGB(),
                    (float) entry.config.tracerWidth);
        }
    }

    private void drawOverlays(ImmutableList<EntityEspConfig> list, RenderWorldLastEvent event) {
//        if (overlayEntityStates.isEmpty()) {
//            return;
//        }
//
//        Vec3d cameraPos = event.getCameraPos();
//        double camX = cameraPos.x;
//        double camY = cameraPos.y;
//        double camZ = cameraPos.z;
//
//        PoseStack poseStack = new PoseStack();
//        EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
//        EntityEspOverlayRenderer renderer = EntityEspOverlayRenderer.getInstance();
//        for (EntityEspConfig config : list) {
//            List<EntityRenderState> states = overlayEntityStates.get(config);
//            if (states == null || states.isEmpty()) {
//                continue;
//            }
//
//            renderer.begin();
//            submitEntityMasks(states, event, renderDispatcher, poseStack, camX, camY, camZ);
//            drawSubmittedMasks();
//            renderer.end(config.overlayColor);
//        }
    }

    private void drawOutlines(ImmutableList<EntityEspConfig> list, RenderWorldLastEvent event) {
//        if (outlineEntityStates.isEmpty()) {
//            return;
//        }
//
//        Vec3 cameraPos = event.getCameraPos();
//        double camX = cameraPos.x();
//        double camY = cameraPos.y();
//        double camZ = cameraPos.z();
//
//        PoseStack poseStack = new PoseStack();
//        EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
//        EntityEspOutlineRenderer renderer = EntityEspOutlineRenderer.getInstance();
//        for (EntityEspConfig config : list) {
//            List<EntityRenderState> states = outlineEntityStates.get(config);
//            if (states == null || states.isEmpty()) {
//                continue;
//            }
//
//            renderer.begin();
//            submitEntityMasks(states, event, renderDispatcher, poseStack, camX, camY, camZ);
//            drawSubmittedMasks();
//            renderer.end(config.outlineColor);
//        }
    }

//    private void submitEntityMasks(
//            List<EntityRenderState> states,
//            RenderWorldLastEvent event,
//            EntityRenderDispatcher renderDispatcher,
//            PoseStack poseStack,
//            double camX,
//            double camY,
//            double camZ
//    ) {
//        for (EntityRenderState state : states) {
//            int outlineColor = state.outlineColor;
//            state.outlineColor = -1;
//            renderDispatcher.submit(state, event.getCameraRenderState(), state.x - camX, state.y - camY, state.z - camZ, poseStack, submitNodeStorage);
//            state.outlineColor = outlineColor;
//        }
//    }
//
//    private void drawSubmittedMasks() {
//        try (FeatureRenderDispatcher.PreparedFrame frame = dispatcher.prepareFrame(submitNodeStorage)) {
//            frame.executeOutline();
//        }
//    }

    private static final class MatchedEntity {

        public final Entity entity;
        public final EntityEspConfig config;

        private MatchedEntity(Entity entity, EntityEspConfig config) {
            this.entity = entity;
            this.config = config;
        }
    }

    private final class BufferedVerticesEntry {

        public final ResourceLocation texture;
        public final FloatList vertices;

        private BufferedVerticesEntry(ResourceLocation texture, FloatList vertices) {
            this.texture = texture;
            this.vertices = vertices;
        }
    }
}