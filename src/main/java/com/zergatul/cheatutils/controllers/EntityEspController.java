package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.TracerConfigBase;
import com.zergatul.cheatutils.utils.SharedVertexBuffer;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

public class EntityEspController {

    public static final EntityEspController instance = new EntityEspController();

    private final Minecraft mc = Minecraft.getInstance();

    private EntityEspController() {
        ModApiWrapper.RenderWorldLast.add(this::render);
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

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

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
                            c.clazz.isInstance(entity) &&
                            distanceSqr < c.getOutlineMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                renderEntityBounding(buffer, view, partialTicks, entity, config);
            }

            config = list.stream().filter(c ->
                    c.enabled &&
                            c.drawTracers &&
                            c.clazz.isInstance(entity) &&
                            distanceSqr < c.getTracerMaxDistanceSqr()).findFirst().orElse(null);

            if (config != null) {
                drawTracer(
                        buffer,
                        view,
                        tracerX, tracerY, tracerZ,
                        entity.getPosition(event.getTickDelta()),
                        config);
            }
        }

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(buffer.end());

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    private static void renderEntityBounding(BufferBuilder buffer, Vec3 view, float partialTicks, Entity entity, EntityTracerConfig config) {
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

        buffer.vertex(p1x - view.x, posY1 - view.y, p1z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x - view.x, posY1 - view.y, p2z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x - view.x, posY1 - view.y, p2z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x - view.x, posY1 - view.y, p3z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x - view.x, posY1 - view.y, p3z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x - view.x, posY1 - view.y, p4z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x - view.x, posY1 - view.y, p4z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p1x - view.x, posY1 - view.y, p1z - view.z).color(r, g, b, 1f).endVertex();

        buffer.vertex(p1x - view.x, posY1 - view.y, p1z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p1x - view.x, posY2 - view.y, p1z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x - view.x, posY1 - view.y, p2z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x - view.x, posY2 - view.y, p2z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x - view.x, posY1 - view.y, p3z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x - view.x, posY2 - view.y, p3z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x - view.x, posY1 - view.y, p4z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x - view.x, posY2 - view.y, p4z - view.z).color(r, g, b, 1f).endVertex();

        buffer.vertex(p1x - view.x, posY2 - view.y, p1z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x - view.x, posY2 - view.y, p2z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x - view.x, posY2 - view.y, p2z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x - view.x, posY2 - view.y, p3z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x - view.x, posY2 - view.y, p3z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x - view.x, posY2 - view.y, p4z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x - view.x, posY2 - view.y, p4z - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p1x - view.x, posY2 - view.y, p1z - view.z).color(r, g, b, 1f).endVertex();
    }

    private static void drawTracer(BufferBuilder buffer, Vec3 view, double tx, double ty, double tz, Vec3 pos, TracerConfigBase config) {
        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;

        buffer.vertex(tx - view.x, ty - view.y, tz - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(pos.x - view.x, pos.y - view.y, pos.z - view.z).color(r, g, b, 1f).endVertex();
    }
}