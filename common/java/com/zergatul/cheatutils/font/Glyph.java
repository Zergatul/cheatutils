package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.gl.AtlasTexture;

public class Glyph {

    public static final Glyph EMPTY = new Glyph(0, 0, 0, 0, 0, 0,null);

    private final float x0;
    private final float y0;
    private final float width;
    private final float height;
    private final float advance;
    private final float leftSideBearing;
    private final AtlasTexture.Item sprite;

    public Glyph(float x, float y, float width, float height, float advance, float leftSideBearing, AtlasTexture.Item sprite) {
        this.x0 = x;
        this.y0 = y;
        this.width = width;
        this.height = height;
        this.advance = advance;
        this.leftSideBearing = leftSideBearing;
        this.sprite = sprite;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getX0() {
        return x0;
    }

    public float getX1() {
        return x0 + width;
    }

    public float getY0() {
        return y0;
    }

    public float getY1() {
        return y0 + height;
    }

    public boolean isBlank() {
        return width == 0;
    }

    public float getAdvanceWidth() {
        return advance;
    }

    public float getLeftSideBearing() {
        return leftSideBearing;
    }

    public AtlasTexture.Item getSprite() {
        return sprite;
    }
}