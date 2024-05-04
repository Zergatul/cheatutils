package com.zergatul.cheatutils.common.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;

public record RenderWorldLayerEvent(Matrix4f pose, Matrix4f projection, Camera camera) {

    public Camera getCamera() {
        return camera;
    }

    public Matrix4f getPose() {
        return pose;
    }

    public Matrix4f getProjection() {
        return projection;
    }
}