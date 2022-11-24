package com.zergatul.cheatutils.wrappers.events;

import com.mojang.blaze3d.vertex.PoseStack;

public record PostRenderGuiEvent(PoseStack matrixStack, float tickDelta) {

    public PoseStack getMatrixStack() {
        return matrixStack;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}