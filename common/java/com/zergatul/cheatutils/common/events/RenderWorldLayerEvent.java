package com.zergatul.cheatutils.common.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;

public record RenderWorldLayerEvent(PoseStack matrixStack, Matrix4f projectionMatrix, Camera camera) {

    public Camera getCamera() {
        return camera;
    }

    public PoseStack getMatrixStack() {
        return matrixStack;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}