package com.zergatul.cheatutils.common.events;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

public class RenderWorldLastEvent {

    public static RenderWorldLastEvent last;

    private final Matrix4f pose;
    private final Matrix4f projection;
    private final Matrix4f mvp;
    private final float tickDelta;
    private final Vec3 tracerCenter;
    private final Vec3 playerPos;
    private final Camera camera;

    public RenderWorldLastEvent(Camera camera, Matrix4f pose, Matrix4f projection, DeltaTracker delta) {
        this.camera = camera;
        this.pose = new Matrix4f(pose);
        this.projection = new Matrix4f(projection);
        this.tickDelta = delta.getGameTimeDeltaPartialTick(true);
        this.mvp = new Matrix4f(projection).mul(pose);
        this.tracerCenter = calculateTracerCenter();
        this.playerPos = camera.entity().getPosition(tickDelta);

        last = this;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Matrix4f getPose() {
        return pose;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public Matrix4f getMvp() {
        return mvp;
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

    private Vec3 calculateTracerCenter() {
        Matrix4f invProjection = new Matrix4f(projection).invert();

        Vector4f ndcNear = new Vector4f(0f, 0f, -1f, 1f);
        Vector4f ndcFar  = new Vector4f(0f, 0f,  1f, 1f);

        Vector4f vNear = invProjection.transform(new Vector4f(ndcNear));
        Vector4f vFar  = invProjection.transform(new Vector4f(ndcFar));

        vNear.div(vNear.w);
        vFar.div(vFar.w);

        Vector3f directionView = new Vector3f(vFar.x - vNear.x, vFar.y - vNear.y, vFar.z - vNear.z).normalize();

        Quaternionf rotation = camera.rotation();
        Vector3f directionWorld = new Matrix3f().rotation(rotation).transform(directionView, new Vector3f());

        Vec3 camPos = camera.position();

        final double distance = 1024;
        double cx = camPos.x + directionWorld.x * distance;
        double cy = camPos.y + directionWorld.y * distance;
        double cz = camPos.z + directionWorld.z * distance;

        return new Vec3(cx, cy, cz);
    }
}