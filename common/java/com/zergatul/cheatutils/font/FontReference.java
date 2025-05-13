package com.zergatul.cheatutils.font;

public abstract class FontReference {
    public abstract GlyphRenderer createGlyphRenderer(FontRenderParameters parameters);
    public abstract FontRenderer createFontRenderer(FontRenderParameters parameters);
}