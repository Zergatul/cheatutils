package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.TracerConfigBase;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

public class RenderController {

    public static final RenderController instance = new RenderController();

    private final Minecraft mc = Minecraft.getInstance();

    private RenderController() {
        ModApiWrapper.addOnRenderWorldLast(this::onRenderWorldLastEvent);
    }

    private void onRenderWorldLastEvent(ModApiWrapper.RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        Vector3d view = camera.getPosition();
        float xRot = camera.getXRot();
        float yRot = camera.getYRot();

        double tracerX = view.x;
        double tracerY = view.y;
        double tracerZ = view.z;
        double playerX = MathHelper.lerp(event.getTickDelta(), mc.player.xOld, mc.player.getX());
        double playerY = MathHelper.lerp(event.getTickDelta(), mc.player.yOld, mc.player.getY());
        double playerZ = MathHelper.lerp(event.getTickDelta(), mc.player.zOld, mc.player.getZ());

        if (true) {
            double deltaXRot = 0;
            double deltaZRot = 0;
            double translateX = 0;
            double translateY = 0;
            if (mc.options.bobView && mc.getCameraEntity() instanceof ClientPlayerEntity) {
                ClientPlayerEntity player = (ClientPlayerEntity) mc.getCameraEntity();
                float f = player.walkDist - player.walkDistO;
                float f1 = -(player.walkDist + f * event.getTickDelta());
                float f2 = MathHelper.lerp(event.getTickDelta(), player.oBob, player.bob);
                //p_228383_1_.translate((double)(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F), (double)(-Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2)), 0.0D);
                //p_228383_1_.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F));
                //p_228383_1_.mulPose(Vector3f.XP.rotationDegrees(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
                translateX = (MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F);
                translateY = (-Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2));
                deltaZRot = MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F;
                deltaXRot = Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F;
            }
            double drawBeforeCameraDist = 64;
            double yaw = yRot * Math.PI / 180;
            double pitch = (xRot + deltaXRot) * Math.PI / 180;

            tracerY -= translateY;
            tracerX += translateX * Math.cos(yaw);
            tracerZ += translateX * Math.sin(yaw);

            tracerX -= Math.sin(yaw) * Math.cos(pitch) * drawBeforeCameraDist;
            tracerZ += Math.cos(yaw) * Math.cos(pitch) * drawBeforeCameraDist;
            tracerY -= Math.sin(pitch) * drawBeforeCameraDist;
        }

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        /**/

        MatrixStack stack = event.getMatrixStack();

        GL11.glPushMatrix();
        RenderSystem.multMatrix(stack.last().pose());

        /**/

        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        renderBlocks(buffer, view, tracerX, tracerY, tracerZ, playerX, playerY, playerZ);
        renderEntities(buffer, view, mc, event.getTickDelta(), tracerX, tracerY, tracerZ, playerX, playerY, playerZ);
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        {
            GL11.glPopMatrix();
        }
    }

    private static void renderBlocks(BufferBuilder buffer, Vector3d view, double tracerX, double tracerY, double tracerZ, double playerX, double playerY, double playerZ) {
        List<BlockTracerConfig> list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {
            for (BlockTracerConfig config: list) {

                if (!config.enabled) {
                    continue;
                }

                ResourceLocation id = ModApiWrapper.BLOCKS.getKey(config.block);

                synchronized (BlockFinderController.instance.blocks) {
                    HashSet<BlockPos> set = BlockFinderController.instance.blocks.get(id);
                    if (set == null) {
                        continue;
                    }

                    for (BlockPos pos : set) {
                        if (config.maxDistance != Double.MAX_VALUE) {
                            double dx = pos.getX() - playerX;
                            double dy = pos.getY() - playerY;
                            double dz = pos.getZ() - playerZ;
                            if (dx * dx + dy * dy + dz * dz > config.maxDistance * config.maxDistance) {
                                continue;
                            }
                        }

                        if (config.drawOutline) {
                            renderBlockBounding(buffer, view, pos, config);
                        }

                        if (config.drawTracers) {
                            drawTracer(
                                buffer,
                                view,
                                tracerX, tracerY, tracerZ,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                config);
                        }

                    }
                }
            }
        }
    }

    private static void renderEntities(BufferBuilder buffer, Vector3d view, Minecraft mc, float partialTicks, double tracerX, double tracerY, double tracerZ, double playerX, double playerY, double playerZ) {
        List<EntityTracerConfig> list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            for (Entity entity : mc.player.clientLevel.entitiesForRendering()) {

                if (entity instanceof ClientPlayerEntity) {
                    continue;
                }

                double dx = entity.getX() - playerX;
                double dy = entity.getY() - playerY;
                double dz = entity.getZ() - playerZ;
                double distance2 = dx * dx + dy * dy + dz * dz;

                EntityTracerConfig config = list.stream().filter(c ->
                        c.enabled &&
                        c.drawOutline &&
                        c.clazz.isInstance(entity) &&
                        distance2 < c.maxDistance * c.maxDistance).findFirst().orElse(null);

                if (config != null) {
                    renderEntityBounding(buffer, view, partialTicks, entity, config);
                }

                config = list.stream().filter(c ->
                        c.enabled &&
                        c.drawTracers &&
                        c.clazz.isInstance(entity) &&
                        distance2 < c.maxDistance * c.maxDistance).findFirst().orElse(null);

                if (config != null) {
                    drawTracer(
                        buffer,
                        view,
                        tracerX, tracerY, tracerZ,
                        getEntityX(entity, partialTicks), getEntityY(entity, partialTicks), getEntityZ(entity, partialTicks),
                        config);
                }
            }
        }
    }

    private static void renderBlockBounding(BufferBuilder buffer, Vector3d view, BlockPos pos, BlockTracerConfig config) {
        int x1 = pos.getX();
        int y1 = pos.getY();
        int z1 = pos.getZ();
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int z2 = z1 + 1;


        float r = config.outlineColor.getRed() / 255f;
        float g = config.outlineColor.getGreen() / 255f;
        float b = config.outlineColor.getBlue() / 255f;

        // bottom
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();

        // top
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();

        // side
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1).endVertex();
    }

    private static void renderEntityBounding(BufferBuilder buffer, Vector3d view, float partialTicks, Entity entity, EntityTracerConfig config) {
        double rotationYaw = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);
        double sin = Math.sin(rotationYaw / 180 * Math.PI);
        double cos = Math.cos(rotationYaw / 180 * Math.PI);
        double width = entity.getBbWidth() / 2;
        double height = entity.getBbHeight();

        double posX = getEntityX(entity, partialTicks);
        double posY = getEntityY(entity, partialTicks);
        double posZ = getEntityZ(entity, partialTicks);

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

        buffer.vertex(p1x - view.x, posY1 - view.y, p1z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p2x - view.x, posY1 - view.y, p2z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p2x - view.x, posY1 - view.y, p2z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p3x - view.x, posY1 - view.y, p3z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p3x - view.x, posY1 - view.y, p3z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p4x - view.x, posY1 - view.y, p4z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p4x - view.x, posY1 - view.y, p4z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p1x - view.x, posY1 - view.y, p1z - view.z).color(r, g, b, 1).endVertex();

        buffer.vertex(p1x - view.x, posY1 - view.y, p1z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p1x - view.x, posY2 - view.y, p1z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p2x - view.x, posY1 - view.y, p2z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p2x - view.x, posY2 - view.y, p2z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p3x - view.x, posY1 - view.y, p3z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p3x - view.x, posY2 - view.y, p3z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p4x - view.x, posY1 - view.y, p4z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p4x - view.x, posY2 - view.y, p4z - view.z).color(r, g, b, 1).endVertex();

        buffer.vertex(p1x - view.x, posY2 - view.y, p1z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p2x - view.x, posY2 - view.y, p2z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p2x - view.x, posY2 - view.y, p2z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p3x - view.x, posY2 - view.y, p3z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p3x - view.x, posY2 - view.y, p3z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p4x - view.x, posY2 - view.y, p4z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p4x - view.x, posY2 - view.y, p4z - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(p1x - view.x, posY2 - view.y, p1z - view.z).color(r, g, b, 1).endVertex();
    }

    private static void drawTracer(BufferBuilder buffer, Vector3d view, double tx, double ty, double tz, double x, double y, double z, TracerConfigBase config) {
        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;

        buffer.vertex(tx - view.x, ty - view.y, tz - view.z).color(r, g, b, 1).endVertex();
        buffer.vertex(x - view.x, y - view.y, z - view.z).color(r, g, b, 1).endVertex();
    }

    private static double getEntityX(Entity entity, float partialTicks) {
        return MathHelper.lerp(partialTicks, entity.xo, entity.getX());
    }

    private static double getEntityY(Entity entity, float partialTicks) {
        return MathHelper.lerp(partialTicks, entity.yo, entity.getY());
    }

    private static double getEntityZ(Entity entity, float partialTicks) {
        return MathHelper.lerp(partialTicks, entity.zo, entity.getZ());
    }
}