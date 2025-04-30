package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.AtlasTexture;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public abstract class Font {
    public abstract Glyph createGlyph(AtlasTexture texture, char ch, float size);
    public abstract TextBounds getSize(Int2ObjectMap<Glyph> glyphs, String text);
    public abstract int render(TextureColor2dRenderer renderer, Int2ObjectMap<Glyph> glyphs, String text, int x, int y, float r, float g, float b, float a);
    public abstract int getLineHeight(float size);
}