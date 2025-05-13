package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.gl.AtlasTexture;

public abstract class GlyphRenderer {
    public abstract Glyph create(AtlasTexture texture, char ch);
}