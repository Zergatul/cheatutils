package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.AtlasTexture;

import java.util.Map;

public abstract class Font {
    public abstract Glyph createGlyph(AtlasTexture texture, char ch, float size);
    public abstract TextBounds getSize(Map<Character, Glyph> glyphs, String text);
    public abstract int render(TextureColor2dRenderer renderer, Map<Character, Glyph> glyphs, String text, int x, int y, float r, float g, float b, float a);
}