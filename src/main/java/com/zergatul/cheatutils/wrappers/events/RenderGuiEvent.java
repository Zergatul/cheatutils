package com.zergatul.cheatutils.wrappers.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;

public record RenderGuiEvent(PoseStack matrixStack, RenderWorldLastEvent renderWorldLastEvent) {

    public PoseStack getMatrixStack() {
        return matrixStack;
    }

    public float getTickDelta() {
        return renderWorldLastEvent.getTickDelta();
    }

    public Matrix4f getWorldPoseMatrix() {
        return renderWorldLastEvent.getPoseMatrix();
    }

    public Matrix4f getWorldProjectionMatrix() {
        return renderWorldLastEvent.getProjectionMatrix();
    }

    public Camera getCamera() {
        return renderWorldLastEvent.getCamera();
    }
}