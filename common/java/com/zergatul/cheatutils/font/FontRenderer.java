package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.buffers.RenderBuffers;

public abstract class FontRenderer implements FontBackendHolder {

    protected static final float SHADOW_FACTOR = 0.25f;

    public abstract TextBounds getTextSize(StylizedText text);
    public abstract float getLineHeight();
    public abstract void drawText(RenderBuffers buffers, StylizedText text, float x, float y);
}