package com.zergatul.cheatutils.common.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class ContainerRenderLabelsEvent {

    private final PoseStack poseStack;
    private final AbstractContainerScreen<?> screen;
    private final int mouseX;
    private final int mouseY;

    public ContainerRenderLabelsEvent(PoseStack poseStack, AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        this.poseStack = poseStack;
        this.screen = screen;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
}