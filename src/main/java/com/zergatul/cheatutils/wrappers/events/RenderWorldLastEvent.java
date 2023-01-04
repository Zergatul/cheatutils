package com.zergatul.cheatutils.wrappers.events;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public record RenderWorldLastEvent(MatrixStack matrixStack, float tickDelta, Matrix4f projectionMatrix) {

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}