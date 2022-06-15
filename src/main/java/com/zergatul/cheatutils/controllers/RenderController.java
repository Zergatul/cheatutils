package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.TracerConfigBase;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashSet;

import static net.minecraft.client.renderer.LevelRenderer.DIRECTIONS;

public class RenderController {

    public static final RenderController instance = new RenderController();

    private Minecraft mc = Minecraft.getInstance();
    private VertexBuffer vertexBuffer;

    private RenderController() {
        RenderSystem.recordRenderCall(() -> vertexBuffer = new VertexBuffer());
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderLevelLastEvent event) {

        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
        //Camera camera = mc.getBlockEntityRenderDispatcher().camera;
        //Vec3 cam = camera.getPosition();

        LightLevelController.instance.render(event);
        EndCityChunksController.instance.render(event);

        double tracerX = view.x;
        double tracerY = view.y;
        double tracerZ = view.z;
        double playerX = Mth.lerp(event.getPartialTick(), mc.player.xOld, mc.player.getX());
        double playerY = Mth.lerp(event.getPartialTick(), mc.player.yOld, mc.player.getY());
        double playerZ = Mth.lerp(event.getPartialTick(), mc.player.zOld, mc.player.getZ());

        if (true) {
            double deltaXRot = 0;
            double deltaZRot = 0;
            double translateX = 0;
            double translateY = 0;
            if (mc.options.bobView().get() && mc.getCameraEntity() instanceof LocalPlayer) {
                LocalPlayer player = (LocalPlayer) mc.getCameraEntity();
                float f = player.walkDist - player.walkDistO;
                float f1 = -(player.walkDist + f * event.getPartialTick());
                float f2 = Mth.lerp(event.getPartialTick(), player.oBob, player.bob);
                //p_228383_1_.translate((double)(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F), (double)(-Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2)), 0.0D);
                //p_228383_1_.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F));
                //p_228383_1_.mulPose(Vector3f.XP.rotationDegrees(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
                translateX = (double)(Mth.sin(f1 * (float)Math.PI) * f2 * 0.5F);
                translateY = (double)(-Math.abs(Mth.cos(f1 * (float)Math.PI) * f2));
                deltaZRot = Mth.sin(f1 * (float)Math.PI) * f2 * 3.0F;
                deltaXRot = Math.abs(Mth.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F;
            }
            double drawBeforeCameraDist = 1;
            double yaw = Mth.lerp(event.getPartialTick(), mc.player.yRotO, mc.player.getYRot()) * Math.PI / 180;
            double pitch = (Mth.lerp(event.getPartialTick(), mc.player.xRotO, mc.player.getXRot()) + deltaXRot) * Math.PI / 180;

            tracerY -= translateY;
            tracerX += translateX * Math.cos(yaw);
            tracerZ += translateX * Math.sin(yaw);

            tracerX -= Math.sin(yaw) * Math.cos(pitch) * drawBeforeCameraDist;
            tracerZ += Math.cos(yaw) * Math.cos(pitch) * drawBeforeCameraDist;
            tracerY -= Math.sin(pitch) * drawBeforeCameraDist;
        }

        var tesselator = Tesselator.getInstance();
        var buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        renderBlocks(buffer, tracerX, tracerY, tracerZ, playerX, playerY, playerZ);
        renderEntities(buffer, mc, event.getPartialTick(), tracerX, tracerY, tracerZ, playerX, playerY, playerZ);

        vertexBuffer.bind();
        vertexBuffer.upload(buffer.end());

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();
        matrix.translate(-view.x, -view.y, -view.z);
        var shader = GameRenderer.getPositionColorShader();
        vertexBuffer.drawWithShader(matrix.last().pose(), event.getProjectionMatrix().copy(), shader);
        matrix.popPose();

        VertexBuffer.unbind();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        drawEnderPearlPath(event, view);
    }

    private static void renderBlocks(BufferBuilder buffer, double tracerX, double tracerY, double tracerZ, double playerX, double playerY, double playerZ) {
        var list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {
            for (BlockTracerConfig config: list) {

                if (!config.enabled) {
                    continue;
                }

                ResourceLocation id = Registry.BLOCK.getKey(config.block);

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
                            renderBlockBounding(buffer, pos, config);
                        }

                        if (config.drawTracers) {
                            drawTracer(
                                buffer,
                                tracerX, tracerY, tracerZ,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                config);
                        }

                    }
                }
            }
        }
    }

    private static void renderEntities(BufferBuilder buffer, Minecraft mc, float partialTicks, double tracerX, double tracerY, double tracerZ, double playerX, double playerY, double playerZ) {
        var list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            for (Entity entity : mc.player.clientLevel.entitiesForRendering()) {

                if (entity instanceof LocalPlayer) {
                    continue;
                }
                if (entity instanceof FreeCamController.ShadowCopyPlayer) {
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
                    renderEntityBounding(buffer, partialTicks, entity, config);
                }

                config = list.stream().filter(c ->
                        c.enabled &&
                        c.drawTracers &&
                        c.clazz.isInstance(entity) &&
                        distance2 < c.maxDistance * c.maxDistance).findFirst().orElse(null);

                if (config != null) {
                    drawTracer(
                        buffer,
                        tracerX, tracerY, tracerZ,
                        getEntityX(entity, partialTicks), getEntityY(entity, partialTicks), getEntityZ(entity, partialTicks),
                        config);
                }
            }
        }
    }

    private static void renderBlockBounding(BufferBuilder buffer, BlockPos pos, BlockTracerConfig config) {

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
        buffer.vertex(x1, y1, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y1, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y1, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y1, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y1, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y1, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y1, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y1, z1).color(r, g, b, 1f).endVertex();

        // top
        buffer.vertex(x1, y2, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y2, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y2, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y2, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y2, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y2, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y2, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y2, z1).color(r, g, b, 1f).endVertex();

        // side
        buffer.vertex(x1, y1, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y2, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y1, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1, y2, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y1, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y2, z2).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y1, z1).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2, y2, z1).color(r, g, b, 1f).endVertex();
    }

    private static void renderEntityBounding(BufferBuilder buffer, float partialTicks, Entity entity, EntityTracerConfig config) {

        double rotationYaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
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

        buffer.vertex(p1x, posY1, p1z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x, posY1, p2z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x, posY1, p2z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x, posY1, p3z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x, posY1, p3z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x, posY1, p4z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x, posY1, p4z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p1x, posY1, p1z).color(r, g, b, 1f).endVertex();

        buffer.vertex(p1x, posY1, p1z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p1x, posY2, p1z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x, posY1, p2z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x, posY2, p2z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x, posY1, p3z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x, posY2, p3z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x, posY1, p4z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x, posY2, p4z).color(r, g, b, 1f).endVertex();

        buffer.vertex(p1x, posY2, p1z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x, posY2, p2z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p2x, posY2, p2z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x, posY2, p3z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p3x, posY2, p3z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x, posY2, p4z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p4x, posY2, p4z).color(r, g, b, 1f).endVertex();
        buffer.vertex(p1x, posY2, p1z).color(r, g, b, 1f).endVertex();
    }

    private static void drawTracer(BufferBuilder buffer, double tx, double ty, double tz, double x, double y, double z, TracerConfigBase config) {

        /*short lineStyle = (short)config.tracerLineStyle;
        if (lineStyle != 0) {
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glLineStipple(Math.max(config.tracerLineWidth, 1), lineStyle);
        }*/

        //GL11.glLineWidth(config.tracerLineWidth);

        //setColor(config.tracerColor);

        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;

        buffer.vertex(tx, ty, tz).color(r, g, b, 1f).endVertex();
        buffer.vertex(x, y, z).color(r, g, b, 1f).endVertex();

        /*if (lineStyle != 0) {
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
        }*/
    }

    private static double getEntityX(Entity entity, float partialTicks) {
        return Mth.lerp(partialTicks, entity.xo, entity.getX());
    }

    private static double getEntityY(Entity entity, float partialTicks) {
        return Mth.lerp(partialTicks, entity.yo, entity.getY());
    }

    private static double getEntityZ(Entity entity, float partialTicks) {
        return Mth.lerp(partialTicks, entity.zo, entity.getZ());
    }

    private void drawEnderPearlPath(RenderLevelLastEvent event, Vec3 view) {
        if (EnderPearlPathController.instance.shouldDrawPath()) {
            float partialTick = event.getPartialTick();

            double x = getEntityX(mc.player, partialTick);
            double y = getEntityY(mc.player, partialTick) + mc.player.getEyeHeight() - 0.1;
            double z = getEntityZ(mc.player, partialTick);
            float xRot = mc.player.getViewXRot(partialTick);
            float yRot = mc.player.getViewYRot(partialTick);

            double shiftX = -Mth.sin((yRot + 90) * ((float)Math.PI / 180F));
            double shiftZ = Mth.cos((yRot + 90) * ((float)Math.PI / 180F));

            float speedX = -Mth.sin(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
            float speedY = -Mth.sin(xRot * ((float)Math.PI / 180F));
            float speedZ = Mth.cos(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));

            Vec3 movement = new Vec3(speedX, speedY, speedZ).normalize().scale(1.5d);
            /*Vec3 vec = mc.player.getDeltaMovement();
            movement = movement.add(vec.x, mc.player.isOnGround() ? 0.0D : vec.y, vec.z);*/

            var tesselator = Tesselator.getInstance();
            var buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            float stepSize = 0.1f;
            int steps = 1000;
            for (int i = 0; i <= steps; i++) {
                x += movement.x * stepSize;
                y += movement.y * stepSize;
                z += movement.z * stepSize;
                movement = new Vec3(movement.x, movement.y - 0.03F * stepSize, movement.z);
                double px = x + shiftX / (20 + i);
                double py = y;
                double pz = z + shiftZ / (20 + i);

                if (i > 0) {
                    buffer.vertex(px, py, pz).color(1f, 1f, 1f, 1f).endVertex();
                }
                if (i < steps) {
                    buffer.vertex(px, py, pz).color(1f, 1f, 1f, 1f).endVertex();
                }
            }

            vertexBuffer.bind();
            vertexBuffer.upload(buffer.end());

            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(-view.x, -view.y, -view.z);
            var shader = GameRenderer.getPositionColorShader();
            vertexBuffer.drawWithShader(poseStack.last().pose(), event.getProjectionMatrix().copy(), shader);
            poseStack.popPose();

            VertexBuffer.unbind();

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();

            //x -= Math.sin(yRot) * Math.cos(xRot) * drawBeforeCameraDist
        }
    }

}