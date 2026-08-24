package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.InstancedCuboidLineRenderer;
import com.zergatul.cheatutils.render.InstancedTracerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityEsp implements Module {

    public static final EntityEsp instance = new EntityEsp();

    private final Minecraft mc = Minecraft.getInstance();

    private EntityEsp() {
        Events.RenderWorldLast.add(this::render);
        Events.Close.add(this::close);
    }

    private void render(RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        float partialTicks = event.getTickDelta();

        Vec3 cameraPos = event.getCamera().getPosition();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        RenderUtilities utilities = RenderUtilities.instance;
        InstancedCuboidLineRenderer cuboidRenderer = utilities.getInstancedCuboidLineRenderer();
        InstancedTracerRenderer tracerRenderer = utilities.getInstancedTracerRenderer();
        cuboidRenderer.begin();
        tracerRenderer.begin();

        ImmutableList<EntityEspConfig> list = ConfigStore.instance.getConfig().entities.configs;
        for (Entity entity : mc.player.clientLevel.entitiesForRendering()) {
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
                    c.drawBoundingBox &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getBoundingBoxMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                Vec3 pos = entity.getPosition(partialTicks);
                AABB box = entity.getDimensions(entity.getPose()).makeBoundingBox(pos);
                cuboidRenderer.cuboid(
                        (float) (box.minX - cameraX),
                        (float) (box.minY - cameraY),
                        (float) (box.minZ - cameraZ),
                        (float) (box.maxX - cameraX),
                        (float) (box.maxY - cameraY),
                        (float) (box.maxZ - cameraZ),
                        config.boundingBoxColor,
                        (float) config.boundingBoxWidth);
            }

            config = list.stream().filter(c ->
                    c.enabled &&
                    c.drawTracers &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                Vec3 pos = entity.getPosition(partialTicks);
                tracerRenderer.tracer(
                        (float) (pos.x - cameraX),
                        (float) (pos.y - cameraY),
                        (float) (pos.z - cameraZ),
                        config.tracerColor,
                        (float) config.tracerWidth);
            }
        }

        tracerRenderer.end(event);
        cuboidRenderer.end(event);
    }

    private void close() {
        RenderUtilities.instance.getInstancedCuboidLineRenderer().close();
    }
}