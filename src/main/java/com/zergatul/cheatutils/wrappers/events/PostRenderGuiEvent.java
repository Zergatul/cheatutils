package com.zergatul.cheatutils.wrappers.events;

import net.minecraft.client.util.math.MatrixStack;

public record PostRenderGuiEvent(MatrixStack matrixStack, float tickDelta) {

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}