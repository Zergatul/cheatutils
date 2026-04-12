package com.zergatul.cheatutils.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface Element {
    void measure(RenderingContext context);
    int getMeasuredWidth();
    int getMeasuredHeight();
    void layout(int x, int y, int width, int height);
    void render(RenderingContext context);
    default void extract(GuiGraphicsExtractor graphics) {
        throw new AssertionError();
    }
}