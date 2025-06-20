package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphics;

public class ScreenRenderEvent {

    private final GuiGraphics graphics;
    private final int mouseX;
    private final int mouseY;

    public ScreenRenderEvent(GuiGraphics graphics, int mouseX, int mouseY) {
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public GuiGraphics getGuiGraphics() {
        return graphics;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
}