package com.zergatul.cheatutils.font;

public class VanillaFontReference extends FontReference {

    protected VanillaFontReference() {}

    @Override
    public GlyphRenderer createGlyphRenderer(FontRenderParameters parameters) {
        return new VanillaGlyphRenderer();
    }
}