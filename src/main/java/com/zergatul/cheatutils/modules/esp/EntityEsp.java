package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.List;

public class EntityEsp implements Module {

    public static final EntityEsp INSTANCE = new EntityEsp();

    private final Minecraft mc = Minecraft.getMinecraft();
    private final List<MatchedEntity> bbList = new ArrayList<>();
    private final List<MatchedEntity> tracerList = new ArrayList<>();
    private final Map<EntityEspConfig, List<Entity>> overlayEntities = new IdentityHashMap<>();
    private final Map<EntityEspConfig, List<Entity>> outlineEntities = new IdentityHashMap<>();
    private boolean enabled = true;

    private EntityEsp() {
        Events.AfterRenderWorld.add(this::onAfterRenderWorld);
        Events.LevelUnload.add(this::clearFrame);
        Events.Close.add(EntityMaskRenderer.INSTANCE::close);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public boolean shouldEntityHaveOutline(Entity entity) {
        return findMinecraftOutlineConfig(entity) != null;
    }

    public Integer getOutlineColor(Entity entity) {
        EntityEspConfig config = findMinecraftOutlineConfig(entity);
        return config == null ? null : config.outlineColor.getRGB();
    }

    private EntityEspConfig findMinecraftOutlineConfig(Entity entity) {
        if (!enabled || !EspGlobal.enabled || mc.player == null) {
            return null;
        }
        double distanceSqr = entity.getDistanceSq(mc.player);
        for (EntityEspConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.useMinecraftOutline() && config.isValidEntity(entity) && distanceSqr < config.getOutlineMaxDistanceSqr()) {
                return config;
            }
        }
        return null;
    }

    private void onAfterRenderWorld(RenderWorldLastEvent event) {
        if (EntityMaskRenderer.isRenderingMask()) {
            return;
        }
        clearFrame();

        if (!enabled || !EspGlobal.enabled || mc.world == null || mc.player == null) {
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

        for (Entity entity : mc.world.loadedEntityList) {
            if (!entity.isAddedToWorld()) {
                continue;
            }

            double dx = entity.posX - playerX;
            double dy = entity.posY - playerY;
            double dz = entity.posZ - playerZ;
            double distanceSqr = dx * dx + dy * dy + dz * dz;

            EntityEspConfig overlayConfig = list.stream().filter(c ->
                    c.shouldDrawOverlay() && c.isValidEntity(entity) &&
                    distanceSqr < c.getOverlayMaxDistanceSqr()).findFirst().orElse(null);
            if (overlayConfig != null) {
                overlayEntities.computeIfAbsent(overlayConfig, unused -> new ArrayList<>()).add(entity);
            }

            EntityEspConfig outlineConfig = list.stream().filter(c ->
                    c.enabled && c.useModOutline() && c.isValidEntity(entity) &&
                    distanceSqr < c.getOutlineMaxDistanceSqr()).findFirst().orElse(null);
            if (outlineConfig != null) {
                outlineEntities.computeIfAbsent(outlineConfig, unused -> new ArrayList<>()).add(entity);
            }

            if (entity instanceof EntityPlayerSP) {
                continue;
            }

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
                    c.isValidEntity(entity) &&
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

        try {
            drawMasks(overlayEntities, list, event, false);
            drawMasks(outlineEntities, list, event, true);
        } finally {
            clearFrame();
        }
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

    private void drawMasks(
            Map<EntityEspConfig, List<Entity>> groups,
            ImmutableList<EntityEspConfig> configs,
            RenderWorldLastEvent event,
            boolean outline
    ) {
        for (EntityEspConfig config : configs) {
            List<Entity> entities = groups.get(config);
            if (entities != null && !entities.isEmpty()) {
                EntityMaskRenderer.INSTANCE.render(
                        entities, event, outline ? config.outlineColor : config.overlayColor, outline);
            }
        }
    }

    private void clearFrame() {
        bbList.clear();
        tracerList.clear();
        overlayEntities.clear();
        outlineEntities.clear();
    }

    private static final class MatchedEntity {

        public final Entity entity;
        public final EntityEspConfig config;

        private MatchedEntity(Entity entity, EntityEspConfig config) {
            this.entity = entity;
            this.config = config;
        }
    }

}