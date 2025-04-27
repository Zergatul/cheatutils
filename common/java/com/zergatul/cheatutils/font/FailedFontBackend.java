package com.zergatul.cheatutils.font;

public class FailedFontBackend extends FontBackend {

    private final int size;

    protected FailedFontBackend(float size) {
        this.size = Math.round(size);
    }

    @Override
    public FontRenderer createFontRenderer(FontRenderDetails details) {
        return new FailedFontRenderer(this, size);
    }
}