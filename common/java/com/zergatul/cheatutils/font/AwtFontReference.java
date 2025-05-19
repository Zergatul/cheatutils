package com.zergatul.cheatutils.font;

import java.awt.Font;

public class AwtFontReference extends FontReference {

    private final Font font;

    protected AwtFontReference(Font font) {
        this.font = font;
    }

    @Override
    public GlyphRenderer createGlyphRenderer(FontRenderParameters parameters) {
        return new AwtGlyphRenderer(font, parameters);
    }
}