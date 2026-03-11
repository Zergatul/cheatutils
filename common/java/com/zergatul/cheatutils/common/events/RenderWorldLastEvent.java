package com.zergatul.cheatutils.common.events;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

public class RenderWorldLastEvent {

    private final CameraRenderState cameraState;
    private final Matrix4f modifiedProjectionMatrix;
    private final Matrix4f mvp;
    private final float partialTickTime;
    private final Vec3 tracerCenter;
    private final GuiGraphics graphics;

    public RenderWorldLastEvent(CameraRenderState cameraState, Matrix4f modifiedProjectionMatrix, DeltaTracker delta) {
        this.cameraState = cameraState;
        this.modifiedProjectionMatrix = modifiedProjectionMatrix;
        this.partialTickTime = delta.getGameTimeDeltaPartialTick(true);
        this.mvp = new Matrix4f(getProjection()).mul(cameraState.viewRotationMatrix);
        this.tracerCenter = calculateTracerCenter();

        Minecraft mc = Minecraft.getInstance();
        this.graphics = new GuiGraphics(mc, mc.gameRenderer.getGameRenderState().guiRenderState, 0, 0);
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

    public Vec3 getTracerCenter() {
        return tracerCenter;
    }

    public Vec3 getPlayerPos() {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity != null) {
            return entity.getPosition(partialTickTime);
        } else {
            return cameraState.pos;
        }
    }

    public Vec3 getCameraPos() {
        return cameraState.pos;
    }

    public GuiGraphics getGuiGraphics() {
        return graphics;
    }

    private Vec3 calculateTracerCenter() {
        Matrix4f invProjection = new Matrix4f(getProjection()).invert();

        Vector4f ndcNear = new Vector4f(0f, 0f, -1f, 1f);
        Vector4f ndcFar  = new Vector4f(0f, 0f,  1f, 1f);

        Vector4f vNear = invProjection.transform(new Vector4f(ndcNear));
        Vector4f vFar  = invProjection.transform(new Vector4f(ndcFar));

        vNear.div(vNear.w);
        vFar.div(vFar.w);

        Vector3f directionView = new Vector3f(vFar.x - vNear.x, vFar.y - vNear.y, vFar.z - vNear.z).normalize();

        Quaternionf rotation = cameraState.orientation;
        Vector3f directionWorld = new Matrix3f().rotation(rotation).transform(directionView, new Vector3f());

        Vec3 camPos = cameraState.pos;

        final double distance = 1024;
        double cx = camPos.x + directionWorld.x * distance;
        double cy = camPos.y + directionWorld.y * distance;
        double cz = camPos.z + directionWorld.z * distance;

        return new Vec3(cx, cy, cz);
    }
}