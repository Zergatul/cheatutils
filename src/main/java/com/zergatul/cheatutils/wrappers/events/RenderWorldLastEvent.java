package com.zergatul.cheatutils.wrappers.events;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class RenderWorldLastEvent {

    private final MatrixStack matrixStack;
    private final float tickDelta;
    private final Matrix4f projectionMatrix;
    private final Vec3d tracerCenter;
    private final Vec3d playerPos;
    private final Camera camera;

    public RenderWorldLastEvent(MatrixStack matrixStack, float tickDelta, Matrix4f projectionMatrix) {
        this.matrixStack = matrixStack;
        this.tickDelta = tickDelta;
        this.projectionMatrix = projectionMatrix;

        MinecraftClient mc = MinecraftClient.getInstance();
        camera = mc.gameRenderer.getCamera();
        Vec3d view = camera.getPos();
        float xRot = camera.getPitch();
        float yRot = camera.getYaw();

        double tracerX = view.x;
        double tracerY = view.y;
        double tracerZ = view.z;

        double deltaXRot = 0;
        double deltaZRot = 0;
        double translateX = 0;
        double translateY = 0;
        if (mc.options.getBobView().getValue() && mc.getCameraEntity() instanceof ClientPlayerEntity) {
            ClientPlayerEntity player = (ClientPlayerEntity) mc.getCameraEntity();
            float f = player.horizontalSpeed - player.prevHorizontalSpeed;
            float f1 = -(player.horizontalSpeed + f * tickDelta);
            float f2 = MathHelper.lerp(tickDelta, player.prevStrideDistance, player.strideDistance);
            //p_228383_1_.translate((double)(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F), (double)(-Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2)), 0.0D);
            //p_228383_1_.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F));
            //p_228383_1_.mulPose(Vector3f.XP.rotationDegrees(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
            translateX = (double)(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F);
            translateY = (double)(-Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2));
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

        tracerCenter = new Vec3d(tracerX, tracerY, tracerZ);

        playerPos = mc.player.getLerpedPos(tickDelta);
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Vec3d getTracerCenter() {
        return tracerCenter;
    }

    public Vec3d getPlayerPos() {
        return playerPos;
    }

    public Camera getCamera() {
        return camera;
    }
}