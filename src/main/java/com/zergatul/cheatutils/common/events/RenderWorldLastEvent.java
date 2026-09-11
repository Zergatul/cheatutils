package com.zergatul.cheatutils.common.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Matrix4f;

public class RenderWorldLastEvent {

    private final float partialTicks;
    private final Vec3d playerPos;
    private final Vec3d cameraPos;
    private final Matrix4f mvp;
    private final Matrix4f projection;
    private final Matrix4f modelView;

    public RenderWorldLastEvent(float partialTicks, Matrix4f projection, Matrix4f modelView) {
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