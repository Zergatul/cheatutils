package com.zergatul.cheatutils.common.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class ContainerRenderLabelsEvent {

    private final GuiGraphics graphics;
    private final AbstractContainerScreen<?> screen;
    private final int mouseX;
    private final int mouseY;

    public ContainerRenderLabelsEvent(GuiGraphics graphics, AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        this.graphics = graphics;
        this.screen = screen;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public GuiGraphics getGuiGraphics() {
        return graphics;
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