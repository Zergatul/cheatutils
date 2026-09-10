package com.zergatul.cheatutils.common.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Matrix4f;

public class RenderWorldLastEvent {

    private final Vec3d playerPos;
    private final Vec3d cameraPos;
    private final Matrix4f mvp;

    public RenderWorldLastEvent(float partialTicks, Matrix4f mvp) {
        Minecraft mc = Minecraft.getMinecraft();
        this.playerPos = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);
        Entity camera = mc.getRenderViewEntity();
        // Vanilla's model-view already includes eye height and third-person offsets.
        this.cameraPos = new Vec3d(
                camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks,
                camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks,
                camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks);
        this.mvp = mvp;
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
}