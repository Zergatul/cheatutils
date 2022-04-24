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
    private VertexBuffer vertexBuffer = new VertexBuffer();

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderLevelLastEvent event) {

        if (!ConfigStore.instance.esp) {
            return;
        }

        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
        //Camera camera = mc.getBlockEntityRenderDispatcher().camera;
        //Vec3 cam = camera.getPosition();

        /*if (ConfigStore.instance.lightLevelConfig.active && ConfigStore.instance.lightLevelConfig.enabled) {

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);

            double maxDistance2 = ConfigStore.instance.lightLevelConfig.maxDistance * ConfigStore.instance.lightLevelConfig.maxDistance;

            for (BlockPos pos : LightLevelController.instance.getBlockForRendering()) {
                double dx = mc.player.getX() - pos.getX();
                double dz = mc.player.getZ() - pos.getZ();
                if (dx * dx + dz * dz > maxDistance2) {
                    continue;
                }
                int blockLight = mc.level.getBrightness(LightLayer.BLOCK, pos);
                int skyLight = mc.level.getBrightness(LightLayer.SKY, pos);
                if (blockLight < 8) {
                    BufferBuilder bufferBuilder = tesselator.getBuilder();
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

                    if (skyLight < 8) {
                        // can spawn any time
                        setColor(new Color(Color.RED.getRGB() & 0xFFFFFF | 0x40000000, true));
                    } else {
                        // can spawn at night
                        setColor(new Color(Color.BLUE.getRGB() & 0xFFFFFF | 0x40000000, true));
                    }

                    double x1 = pos.getX();
                    double x2 = x1 + 1;
                    double z1 = pos.getZ();
                    double z2 = z1 + 1;
                    double y = pos.getY() + 0.1d;
                    bufferBuilder.vertex(x1, y, z1).endVertex();
                    bufferBuilder.vertex(x1, y, z2).endVertex();
                    bufferBuilder.vertex(x2, y, z2).endVertex();
                    bufferBuilder.vertex(x2, y, z1).endVertex();
                    tesselator.end();
                }
            }

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }*/

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
            if (mc.options.bobView && mc.getCameraEntity() instanceof LocalPlayer) {
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

        buffer.end();

        vertexBuffer.upload(buffer);

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

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void renderold(RenderLevelLastEvent event) {
        if (!ConfigStore.instance.esp) {
            return;
        }

        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
        //Camera camera = mc.getBlockEntityRenderDispatcher().camera;
        //Vec3 cam = camera.getPosition();

        /*if (ConfigStore.instance.lightLevelConfig.active && ConfigStore.instance.lightLevelConfig.enabled) {

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);

            double maxDistance2 = ConfigStore.instance.lightLevelConfig.maxDistance * ConfigStore.instance.lightLevelConfig.maxDistance;

            for (BlockPos pos : LightLevelController.instance.getBlockForRendering()) {
                double dx = mc.player.getX() - pos.getX();
                double dz = mc.player.getZ() - pos.getZ();
                if (dx * dx + dz * dz > maxDistance2) {
                    continue;
                }
                int blockLight = mc.level.getBrightness(LightLayer.BLOCK, pos);
                int skyLight = mc.level.getBrightness(LightLayer.SKY, pos);
                if (blockLight < 8) {
                    BufferBuilder bufferBuilder = tesselator.getBuilder();
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

                    if (skyLight < 8) {
                        // can spawn any time
                        setColor(new Color(Color.RED.getRGB() & 0xFFFFFF | 0x40000000, true));
                    } else {
                        // can spawn at night
                        setColor(new Color(Color.BLUE.getRGB() & 0xFFFFFF | 0x40000000, true));
                    }

                    double x1 = pos.getX();
                    double x2 = x1 + 1;
                    double z1 = pos.getZ();
                    double z2 = z1 + 1;
                    double y = pos.getY() + 0.1d;
                    bufferBuilder.vertex(x1, y, z1).endVertex();
                    bufferBuilder.vertex(x1, y, z2).endVertex();
                    bufferBuilder.vertex(x2, y, z2).endVertex();
                    bufferBuilder.vertex(x2, y, z1).endVertex();
                    tesselator.end();
                }
            }

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }*/

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
            if (mc.options.bobView && mc.getCameraEntity() instanceof LocalPlayer) {
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

        buffer.end();

        var vertexBuffer = new VertexBuffer();
        vertexBuffer.upload(buffer);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();
        matrix.translate(-view.x, -view.y, -view.z);
        var shader = GameRenderer.getPositionColorShader();
        vertexBuffer.drawWithShader(matrix.last().pose(), event.getProjectionMatrix().copy(), shader);
        matrix.popPose();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    private void rendernew(RenderLevelLastEvent event) {
        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
        //Camera camera = mc.getBlockEntityRenderDispatcher().camera;
        //Vec3 cam = camera.getPosition();

        /*if (ConfigStore.instance.lightLevelConfig.active && ConfigStore.instance.lightLevelConfig.enabled) {

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);

            double maxDistance2 = ConfigStore.instance.lightLevelConfig.maxDistance * ConfigStore.instance.lightLevelConfig.maxDistance;

            for (BlockPos pos : LightLevelController.instance.getBlockForRendering()) {
                double dx = mc.player.getX() - pos.getX();
                double dz = mc.player.getZ() - pos.getZ();
                if (dx * dx + dz * dz > maxDistance2) {
                    continue;
                }
                int blockLight = mc.level.getBrightness(LightLayer.BLOCK, pos);
                int skyLight = mc.level.getBrightness(LightLayer.SKY, pos);
                if (blockLight < 8) {
                    BufferBuilder bufferBuilder = tesselator.getBuilder();
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

                    if (skyLight < 8) {
                        // can spawn any time
                        setColor(new Color(Color.RED.getRGB() & 0xFFFFFF | 0x40000000, true));
                    } else {
                        // can spawn at night
                        setColor(new Color(Color.BLUE.getRGB() & 0xFFFFFF | 0x40000000, true));
                    }

                    double x1 = pos.getX();
                    double x2 = x1 + 1;
                    double z1 = pos.getZ();
                    double z2 = z1 + 1;
                    double y = pos.getY() + 0.1d;
                    bufferBuilder.vertex(x1, y, z1).endVertex();
                    bufferBuilder.vertex(x1, y, z2).endVertex();
                    bufferBuilder.vertex(x2, y, z2).endVertex();
                    bufferBuilder.vertex(x2, y, z1).endVertex();
                    tesselator.end();
                }
            }

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }*/

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
            if (mc.options.bobView && mc.getCameraEntity() instanceof LocalPlayer) {
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
        /*buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        renderBlocks(buffer, tracerX, tracerY, tracerZ, playerX, playerY, playerZ);
        renderEntities(buffer, mc, event.getPartialTick(), tracerX, tracerY, tracerZ, playerX, playerY, playerZ);

        buffer.end();*/

        /*var vertexBuffer = new VertexBuffer();
        vertexBuffer.upload(buffer);*/

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(event.getPoseStack().last().pose());
        RenderSystem.applyModelViewMatrix();

        posestack.pushPose();
        posestack.translate(-view.x, -view.y, -view.z);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(5.0F);

        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        buffer.vertex(0, 0, 0).color(255, 255, 255, 255).normal(-1, -1, -1).endVertex();
        buffer.vertex(255, 255, 255).color(255, 255, 255, 255).normal(-1, -1, -1).endVertex();
        tesselator.end();

        RenderSystem.lineWidth(1.0F);

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        posestack.popPose();
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void test(PoseStack matrix, Matrix4f proj) {

        PoseStack posestack1 = RenderSystem.getModelViewStack();
        {
            posestack1.pushPose();
            posestack1.mulPoseMatrix(matrix.last().pose());
            RenderSystem.applyModelViewMatrix();
        }

        Camera p_109794_ = Minecraft.getInstance().gameRenderer.getMainCamera();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (true) {
            double d0 = p_109794_.getPosition().x();
            double d1 = p_109794_.getPosition().y();
            double d2 = p_109794_.getPosition().z();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();

            for (var chunk : ChunkController.instance.loadedChunks) {
                BlockPos blockpos = new BlockPos(chunk.getPos().x * 16, 128, chunk.getPos().z * 16);
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.translate((double) blockpos.getX() - d0, (double) blockpos.getY() - d1, (double) blockpos.getZ() - d2);
                RenderSystem.applyModelViewMatrix();
                RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
                if (true) {
                    bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                    RenderSystem.lineWidth(5.0F);
                    int i = 0;
                    int j = i >> 16 & 255;
                    int k = i >> 8 & 255;
                    int l = i & 255;

                    for (int i1 = 0; i1 < DIRECTIONS.length; ++i1) {
                        if (true) {
                            Direction direction = DIRECTIONS[i1];
                            bufferbuilder.vertex(8.0D, 8.0D, 8.0D).color(j, k, l, 255).normal((float) direction.getStepX(), (float) direction.getStepY(), (float) direction.getStepZ()).endVertex();
                            bufferbuilder.vertex((double) (8 - 16 * direction.getStepX()), (double) (8 - 16 * direction.getStepY()), (double) (8 - 16 * direction.getStepZ())).color(j, k, l, 255).normal((float) direction.getStepX(), (float) direction.getStepY(), (float) direction.getStepZ()).endVertex();
                        }
                    }

                    tesselator.end();
                    RenderSystem.lineWidth(1.0F);
                }

                posestack.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
        }

        {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            posestack1.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    /*@SubscribeEvent
    public void onEvent(RenderGameOverlayEvent.Text event) throws IllegalAccessException {
        MatrixStack matrixStack = event.getMatrixStack();
        if (this.mc.options.renderDebug) {
            float top = mc.getWindow().getGuiScaledHeight() / 2;
            float left = mc.getWindow().getGuiScaledWidth() / 2;

            Format decimalFormat = new DecimalFormat("0.00000");

            mc.font.draw(matrixStack, "translateX=" + decimalFormat.format(_translateX), left, top, 14737632);
            top += mc.font.lineHeight;
            mc.font.draw(matrixStack, "translateY=" + decimalFormat.format(_translateY), left, top, 14737632);
            top += mc.font.lineHeight;
            mc.font.draw(matrixStack, "deltaXRot=" + decimalFormat.format(_deltaXRot), left, top, 14737632);
            top += mc.font.lineHeight;
            mc.font.draw(matrixStack, "deltaZRot=" + decimalFormat.format(_deltaZRot), left, top, 14737632);
        }
    }*/

    private static void renderBlocks(BufferBuilder buffer, double tracerX, double tracerY, double tracerZ, double playerX, double playerY, double playerZ) {

        synchronized (ConfigStore.instance.blocks) {
            for (BlockTracerConfig config : ConfigStore.instance.blocks) {

                if (!config.enabled) {
                    continue;
                }

                ResourceLocation id = config.block.getRegistryName();

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

        synchronized (ConfigStore.instance.entities) {
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

                EntityTracerConfig config = ConfigStore.instance.entities.stream().filter(c ->
                        c.enabled &&
                        c.drawOutline &&
                        c.clazz.isInstance(entity) &&
                        distance2 < c.maxDistance * c.maxDistance).findFirst().orElse(null);

                if (config != null) {
                    renderEntityBounding(buffer, partialTicks, entity, config);
                }

                config = ConfigStore.instance.entities.stream().filter(c ->
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
        return Mth.lerp(partialTicks, entity.xOld, entity.getX());
    }

    private static double getEntityY(Entity entity, float partialTicks) {
        return Mth.lerp(partialTicks, entity.yOld, entity.getY());
    }

    private static double getEntityZ(Entity entity, float partialTicks) {
        return Mth.lerp(partialTicks, entity.zOld, entity.getZ());
    }

    public static void setColor(Color color) {
        GL11.glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

}