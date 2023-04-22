package com.zergatul.cheatutils.common.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RenderWorldLastEvent {

    public static RenderWorldLastEvent last;

    private final PoseStack matrixStack;
    private final Matrix4f poseMatrix;
    private final float tickDelta;
    private final Matrix4f projectionMatrix;
    private final Vec3 tracerCenter;
    private final Vec3 playerPos;
    private final Camera camera;

    public RenderWorldLastEvent(PoseStack matrixStack, float tickDelta, Matrix4f projectionMatrix) {
        this.matrixStack = matrixStack;
        this.poseMatrix = new Matrix4f(matrixStack.last().pose());
        this.tickDelta = tickDelta;
        this.projectionMatrix = new Matrix4f(projectionMatrix);

        Minecraft mc = Minecraft.getInstance();
        camera = mc.gameRenderer.getMainCamera();
        Vec3 view = camera.getPosition();
        float xRot = camera.getXRot();
        float yRot = camera.getYRot();

        double tracerX = view.x;
        double tracerY = view.y;
        double tracerZ = view.z;

        double deltaXRot = 0;
        double deltaZRot = 0;
        double translateX = 0;
        double translateY = 0;
        if (mc.options.bobView().get() && mc.getCameraEntity() instanceof LocalPlayer) {
            LocalPlayer player = (LocalPlayer) mc.getCameraEntity();
            float f = player.walkDist - player.walkDistO;
            float f1 = -(player.walkDist + f * tickDelta);
            float f2 = Mth.lerp(tickDelta, player.oBob, player.bob);
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

        tracerCenter = new Vec3(tracerX, tracerY, tracerZ);

        playerPos = mc.player.getPosition(tickDelta);

        last = this;
    }

    public PoseStack getMatrixStack() {
        return matrixStack;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Matrix4f getPoseMatrix() {
        return poseMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Vec3 getTracerCenter() {
        return tracerCenter;
    }

    public Vec3 getPlayerPos() {
        return playerPos;
    }

    public Camera getCamera() {
        return camera;
    }
}