package com.zergatul.cheatutils.common.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

public class RenderWorldLastEvent {

    private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    private static RenderWorldLastEvent CAPTURED_EVENT;

    private final float partialTicks;
    private final Vec3d playerPos;
    private final Vec3d cameraPos;
    private final Matrix4f mvp;
    private final Matrix4f projection;
    private final Matrix4f modelView;

    private RenderWorldLastEvent(float partialTicks, Matrix4f projection, Matrix4f modelView) {
        this.partialTicks = partialTicks;
        Minecraft mc = Minecraft.getMinecraft();
        this.playerPos = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);
        Entity camera = mc.getRenderViewEntity();
        // Vanilla's model-view already includes eye height and third-person offsets.
        this.cameraPos = new Vec3d(
                camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks,
                camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks,
                camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks);
        this.projection = projection;
        this.modelView = modelView;
        this.mvp = Matrix4f.mul(projection, modelView, null);
    }

    public static void captureEvent(float partialTicks) {
        MATRIX_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, MATRIX_BUFFER);
        Matrix4f projection = new Matrix4f();
        projection.load(MATRIX_BUFFER);

        MATRIX_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MATRIX_BUFFER);
        Matrix4f modelView = new Matrix4f();
        modelView.load(MATRIX_BUFFER);

        CAPTURED_EVENT = new RenderWorldLastEvent(partialTicks, projection, modelView);
    }

    public static RenderWorldLastEvent releaseEvent() {
        RenderWorldLastEvent event = CAPTURED_EVENT;
        if (event == null) {
            throw new IllegalStateException("RenderWorldLastEvent was not captured due to mod conflict.");
        }
        CAPTURED_EVENT = null;
        return event;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public Vec3d getPlayerPos() {
        return playerPos;
    }

    public Vec3d getCameraPos() {
        return cameraPos;
    }

    public Matrix4f getMvp() {
        return mvp;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public Matrix4f getModelView() {
        return modelView;
    }
}