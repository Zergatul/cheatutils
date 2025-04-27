package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.AtlasTexture;

import java.util.Map;

public class FailedFont extends Font {

    public static final Font instance = new FailedFont();

    @Override
    public Glyph createGlyph(AtlasTexture texture, char ch, float size) {
        return Glyph.EMPTY;
    }

    @Override
    public TextBounds getSize(Map<Character, Glyph> glyphs, String text) {
        return TextBounds.EMPTY;
    }

    @Override
    public int render(TextureColor2dRenderer renderer, Map<Character, Glyph> glyphs, String text, int x, int y, float r, float g, float b, float a) {
        return 0;
    }
}