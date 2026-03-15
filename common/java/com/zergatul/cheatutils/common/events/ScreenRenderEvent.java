package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ScreenRenderEvent {

    private final GuiGraphicsExtractor graphics;
    private final int mouseX;
    private final int mouseY;

    public ScreenRenderEvent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public GuiGraphicsExtractor getGraphics() {
        return graphics;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
}