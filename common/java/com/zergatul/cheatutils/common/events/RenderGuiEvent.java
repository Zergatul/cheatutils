package com.zergatul.cheatutils.common.events;

import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

public record RenderGuiEvent(GuiGraphics graphics, RenderWorldLastEvent renderWorldLastEvent) {

    public GuiGraphics getGuiGraphics() {
        return graphics;
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