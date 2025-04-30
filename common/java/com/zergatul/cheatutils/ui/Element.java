package com.zergatul.cheatutils.ui;

public interface Element {
    void measure(RenderingContext context);
    int getMeasuredWidth();
    int getMeasuredHeight();
    void layout(int x, int y, int width, int height);
    void render(RenderingContext context);
}