package com.zergatul.cheatutils.wrappers.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

public record RenderWorldLastEvent(PoseStack matrixStack, float tickDelta, Matrix4f projectionMatrix) {

    public PoseStack getMatrixStack() {
        return matrixStack;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}