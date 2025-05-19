package com.zergatul.cheatutils.font;

public class StbFontReference extends FontReference {

    private final StbFont font;

    protected StbFontReference(StbFont font) {
        this.font = font;
    }

    @Override
    public GlyphRenderer createGlyphRenderer(FontRenderParameters parameters) {
        return new StbGlyphRenderer(font, parameters);
    }
}