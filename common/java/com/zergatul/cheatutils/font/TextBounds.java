package com.zergatul.cheatutils.font;

public class TextBounds {

    public static final TextBounds EMPTY = new TextBounds(0, 0, 0);

    private final int width;
    private final int y0;
    private final int y1;

    public TextBounds(int width, int y0, int y1) {
        this.width = width;
        this.y0 = y0;
        this.y1 = y1;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return y1 - y0;
    }

    public int getY0() {
        return y0;
    }

    public int getY1() {
        return y1;
    }
}