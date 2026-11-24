package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ScreenRenderEvent {

    private final GuiGraphicsExtractor graphics;

    public ScreenRenderEvent(GuiGraphicsExtractor graphics) {
        this.graphics = graphics;
    }

    public GuiGraphicsExtractor getGraphics() {
        return graphics;
    }
}