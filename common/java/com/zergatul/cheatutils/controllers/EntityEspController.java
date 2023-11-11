package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.TracerConfigBase;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityEspController {

    public static final EntityEspController instance = new EntityEspController();

    private final Minecraft mc = Minecraft.getInstance();

    private EntityEspController() {
        Events.RenderWorldLast.add(this::render);
    }

    private void render(RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        float partialTicks = event.getTickDelta();

        Vec3 view = event.getCamera().getPosition();

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        Vec3 tracerCenter = event.getTracerCenter();
        double tracerX = tracerCenter.x;
        double tracerY = tracerCenter.y;
        double tracerZ = tracerCenter.z;

        LineRenderer renderer = RenderUtilities.instance.getLineRenderer();
        renderer.begin(event, false);

        ImmutableList<EntityTracerConfig> list = ConfigStore.instance.getConfig().entities.configs;
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

            EntityTracerConfig config = list.stream().filter(c ->
                    c.enabled &&
                    c.drawOutline &&
                    c.isValidEntity(entity) &&
                    distanceSqr < c.getOutlineMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                renderEntityBounding(renderer, partialTicks, entity, config);
            }

            config = list.stream().filter(c ->
                    c.enabled &&
                            c.drawTracers &&
                            c.clazz.isInstance(entity) &&
                            distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                drawTracer(
                        renderer,
                        tracerX, tracerY, tracerZ,
                        entity.getPosition(event.getTickDelta()),
                        config);
            }
        }

        renderer.end();
    }

    private static void renderEntityBounding(LineRenderer renderer, float partialTicks, Entity entity, EntityTracerConfig config) {
        double rotationYaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        double sin = Math.sin(rotationYaw / 180 * Math.PI);
        double cos = Math.cos(rotationYaw / 180 * Math.PI);
        double width = entity.getBbWidth() / 2;
        double height = entity.getBbHeight();

        Vec3 pos = entity.getPosition(partialTicks);
        double posX = pos.x;
        double posY = pos.y;
        double posZ = pos.z;

        double p1x = posX + width * (cos - sin);
        double p1z = posZ + width * (sin + cos);
        double p2x = posX + width * (cos + sin);
        double p2z = posZ + width * (sin - cos);
        double p3x = posX + width * (-cos + sin);
        double p3z = posZ - width * (sin + cos);
        double p4x = posX - width * (cos + sin);
        double p4z = posZ + width * (-sin + cos);

        double posY1 = posY;
        double posY2 = posY + height;

        float r = config.outlineColor.getRed() / 255f;
        float g = config.outlineColor.getGreen() / 255f;
        float b = config.outlineColor.getBlue() / 255f;

        renderer.line(p1x, posY1, p1z, p2x, posY1, p2z, r, g, b, 1f);
        renderer.line(p2x, posY1, p2z, p3x, posY1, p3z, r, g, b, 1f);
        renderer.line(p3x, posY1, p3z, p4x, posY1, p4z, r, g, b, 1f);
        renderer.line(p4x, posY1, p4z, p1x, posY1, p1z, r, g, b, 1f);

        renderer.line(p1x, posY1, p1z, p1x, posY2, p1z, r, g, b, 1f);
        renderer.line(p2x, posY1, p2z, p2x, posY2, p2z, r, g, b, 1f);
        renderer.line(p3x, posY1, p3z, p3x, posY2, p3z, r, g, b, 1f);
        renderer.line(p4x, posY1, p4z, p4x, posY2, p4z, r, g, b, 1f);

        renderer.line(p1x, posY2, p1z, p2x, posY2, p2z, r, g, b, 1f);
        renderer.line(p2x, posY2, p2z, p3x, posY2, p3z, r, g, b, 1f);
        renderer.line(p3x, posY2, p3z, p4x, posY2, p4z, r, g, b, 1f);
        renderer.line(p4x, posY2, p4z, p1x, posY2, p1z, r, g, b, 1f);
    }

    private static void drawTracer(LineRenderer renderer, double tx, double ty, double tz, Vec3 pos, TracerConfigBase config) {
        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;

        renderer.line(tx, ty, tz, pos.x, pos.y, pos.z, r, g, b, 1f);
    }
}