package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.render.gl.AtlasTexture;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public abstract class FontRenderer {

    private final AtlasTexture texture;
    private final Int2ObjectMap<Glyph> glyphs;
    private final StringBuilder builder;

    protected FontRenderer() {
        assert RenderSystem.isOnRenderThread();

        this.texture = new AtlasTexture();
        this.glyphs = new Int2ObjectOpenHashMap<>();
        this.builder = new StringBuilder();
    }

    public TextBounds getTextSize(StylizedText text) {
        builder.delete(0, builder.length());
        for (StylizedTextChunk chunk : text.chunks) {
            builder.append(chunk.text());
        }
        return getTextSize(builder.toString());
    }

    public TextBounds getTextSize(String string) {
        if (string == null) {
            return TextBounds.EMPTY;
        }

        //ensureGlyphs(string);
        //return font.getSize(glyphs, string);
        return TextBounds.EMPTY;
    }
}