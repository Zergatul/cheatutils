package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.render.Position2dColorRenderer;

public class RectangleElement implements Element {

    private int width;
    private int height;
    private int color;
    private int x, y;

    public RectangleElement(int width, int height, int color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }

    @Override
    public void measure(RenderingContext context) {}

    @Override
    public int getMeasuredWidth() {
        return width;
    }

    @Override
    public int getMeasuredHeight() {
        return height;
    }

    @Override
    public void layout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(RenderingContext context) {
        Position2dColorRenderer.BufferBuilder buffer = context.getBuffers().getColor2d();
        buffer.rect(x, y, width, height, color);
    }
}
