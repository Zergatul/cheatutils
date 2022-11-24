package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.TracerConfigBase;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Set;

public class RenderController {

    public static final RenderController instance = new RenderController();

    private Minecraft mc = Minecraft.getInstance();
    private VertexBuffer vertexBuffer;

    private RenderController() {
        ModApiWrapper.RenderWorldLast.add(this::onRenderWorldLastEvent);
        RenderSystem.recordRenderCall(() -> vertexBuffer = new VertexBuffer());
    }

    private void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 view = camera.getPosition();
        float xRot = camera.getXRot();
        float yRot = camera.getYRot();

        LightLevelController.instance.render(event);
        EndCityChunksController.instance.render(event);

        double tracerX = view.x;
        double tracerY = view.y;
        double tracerZ = view.z;
        double playerX = Mth.lerp(event.getTickDelta(), mc.player.xOld, mc.player.getX());
        double playerY = Mth.lerp(event.getTickDelta(), mc.player.yOld, mc.player.getY());
        double playerZ = Mth.lerp(event.getTickDelta(), mc.player.zOld, mc.player.getZ());

        if (true) {
            double deltaXRot = 0;
            double deltaZRot = 0;
            double translateX = 0;
            double translateY = 0;
            if (mc.options.bobView().get() && mc.getCameraEntity() instanceof LocalPlayer) {
                LocalPlayer player = (LocalPlayer) mc.getCameraEntity();
                float f = player.walkDist - player.walkDistO;
                float f1 = -(player.walkDist + f * event.getTickDelta());
                float f2 = Mth.lerp(event.getTickDelta(), player.oBob, player.bob);
                //p_228383_1_.translate((double)(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F), (double)(-Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2)), 0.0D);
                //p_228383_1_.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F));
                //p_228383_1_.mulPose(Vector3f.XP.rotationDegrees(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
                translateX = (double)(Mth.sin(f1 * (float)Math.PI) * f2 * 0.5F);
                translateY = (double)(-Math.abs(Mth.cos(f1 * (float)Math.PI) * f2));
                deltaZRot = Mth.sin(f1 * (float)Math.PI) * f2 * 3.0F;
                deltaXRot = Math.abs(Mth.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F;
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

        var tesselator = Tesselator.getInstance();
        var buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        renderBlocks(buffer, view, tracerX, tracerY, tracerZ, playerX, playerY, playerZ);

        /**/
        /*var tempconfig = new BlockTracerConfig();
        tempconfig.outlineColor = Color.GREEN;
        NewChunksController.instance.getNewBlocks().forEach(p -> {
            renderBlockBounding(buffer, view, p, tempconfig);
        });
        tempconfig.outlineColor = Color.RED;
        NewChunksController.instance.getOldBlocks().forEach(p -> {
            renderBlockBounding(buffer, view, p, tempconfig);
        });*/
        /**/

        renderEntities(buffer, view, mc, event.getTickDelta(), tracerX, tracerY, tracerZ, playerX, playerY, playerZ);

        vertexBuffer.bind();
        vertexBuffer.upload(buffer.end());

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        PoseStack matrix = event.getMatrixStack();
        matrix.pushPose();
        //matrix.translate(-view.x, -view.y, -view.z);
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

    private static void renderBlocks(BufferBuilder buffer, Vec3 view, double tracerX, double tracerY, double tracerZ, double playerX, double playerY, double playerZ) {
        for (BlockTracerConfig config: ConfigStore.instance.getConfig().blocks.configs) {
            if (!config.enabled) {
                continue;
            }

            Set<BlockPos> set = BlockFinderController.instance.blocks.get(config.block);
            if (set == null) {
                continue;
            }

            for (BlockPos pos: set) {
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

    private static void renderEntities(BufferBuilder buffer, Vec3 view, Minecraft mc, float partialTicks, double tracerX, double tracerY, double tracerZ, double playerX, double playerY, double playerZ) {
        var list = ConfigStore.instance.getConfig().entities.configs;
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

    private static void renderBlockBounding(BufferBuilder buffer, Vec3 view, BlockPos pos, BlockTracerConfig config) {

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
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();

        // top
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();

        // side
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
    }

    private static void renderEntityBounding(BufferBuilder buffer, Vec3 view, float partialTicks, Entity entity, EntityTracerConfig config) {

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

    private static void drawTracer(BufferBuilder buffer, Vec3 view, double tx, double ty, double tz, double x, double y, double z, TracerConfigBase config) {
        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;

        buffer.vertex(tx - view.x, ty - view.y, tz - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x - view.x, y - view.y, z - view.z).color(r, g, b, 1f).endVertex();
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

    private void drawEnderPearlPath(RenderWorldLastEvent event, Vec3 view) {
        if (EnderPearlPathController.instance.shouldDrawPath()) {
            float partialTick = event.getTickDelta();

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
                    buffer.vertex(px - view.x, py - view.y, pz - view.z).color(1f, 1f, 1f, 1f).endVertex();
                }
                if (i < steps) {
                    buffer.vertex(px - view.x, py - view.y, pz - view.z).color(1f, 1f, 1f, 1f).endVertex();
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

            PoseStack poseStack = event.getMatrixStack();
            poseStack.pushPose();
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