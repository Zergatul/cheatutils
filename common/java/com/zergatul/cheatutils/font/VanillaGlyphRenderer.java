package com.zergatul.cheatutils.font;

public class VanillaGlyphRenderer extends GlyphRenderer {

    protected VanillaGlyphRenderer() {}

    @Override
    public FontRenderer createFontRenderer(FontRenderDetails details) {
        return new VanillaFontRenderer(details.scale(), details.dropShadow());
    }

    @Override
    public float getLineHeight() {
        throw new IllegalStateException();
    }

    @Override
    protected Glyph renderGlyph(char ch) {
        throw new IllegalStateException();
    }
}