package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.AtlasTexture;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class FailedFont extends Font {

    public static final Font instance = new FailedFont();

    @Override
    public Glyph createGlyph(AtlasTexture texture, char ch, float size) {
        return Glyph.EMPTY;
    }

    @Override
    public TextBounds getSize(Int2ObjectMap<Glyph> glyphs, String text) {
        return TextBounds.EMPTY;
    }

    @Override
    public int render(TextureColor2dRenderer renderer, Int2ObjectMap<Glyph> glyphs, String text, int x, int y, float r, float g, float b, float a) {
        return 0;
    }

    @Override
    public int render(FloatList buffer, Int2ObjectMap<Glyph> glyphs, String text, int x, int y, float r, float g, float b, float a) {
        return 0;
    }

    @Override
    public int getLineHeight(float size) {
        return 0;
    }
}