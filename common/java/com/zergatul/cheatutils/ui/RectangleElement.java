package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.render.Color2dRenderer;

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
        Color2dRenderer renderer = context.getColor2dRenderer();
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        renderer.rect(x, y, width, height, r, g, b, a);
    }
}
