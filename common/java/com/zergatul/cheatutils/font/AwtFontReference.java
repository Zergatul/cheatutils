package com.zergatul.cheatutils.font;

import java.awt.Font;

public class AwtFontReference extends FontReference {

    private final Font font;

    public AwtFontReference(Font font) {
        this.font = font;
    }

    @Override
    public GlyphRenderer createGlyphRenderer(FontRenderParameters parameters) {
        return new AwtGlyphRenderer(font, parameters);
    }

    @Override
    public FontRenderer createFontRenderer(FontRenderParameters parameters) {
        return null;
    }
}