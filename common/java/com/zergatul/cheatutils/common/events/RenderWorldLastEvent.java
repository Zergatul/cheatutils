package com.zergatul.cheatutils.common.events;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class RenderWorldLastEvent {

    private final CameraRenderState cameraState;
    private final Matrix4f modifiedProjectionMatrix;
    private final Matrix4f mvp;
    private final float partialTickTime;

    public RenderWorldLastEvent(CameraRenderState cameraState, Matrix4f modifiedProjectionMatrix, DeltaTracker delta) {
        this.cameraState = cameraState;
        this.modifiedProjectionMatrix = modifiedProjectionMatrix;

        this.partialTickTime = delta.getGameTimeDeltaPartialTick(true);
        this.mvp = new Matrix4f(getProjection()).mul(cameraState.viewRotationMatrix);
    }

    public float getPartialTickTime() {
        return partialTickTime;
    }

    public Matrix4f getViewRotation() {
        return cameraState.viewRotationMatrix;
    }

    public Matrix4f getProjection() {
        return modifiedProjectionMatrix;
    }

    public Matrix4f getMvp() {
        return mvp;
    }

    public Vec3 getPlayerPos() {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity != null) {
            return entity.getPosition(partialTickTime);
        } else {
            return cameraState.pos;
        }
    }

    public CameraRenderState getCameraRenderState() {
        return cameraState;
    }

    public Vec3 getCameraPos() {
        return cameraState.pos;
    }
}