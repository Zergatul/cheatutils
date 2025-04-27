package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.AtlasTexture;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class GlyphFontRenderer {

    public final AtlasTexture texture;

    private final String name;
    private final int size;
    private final Map<Character, Glyph> glyphs;
    private final StringBuilder builder;

    private Font font;

    public GlyphFontRenderer(String name, int size) {
        this.name = name;
        this.size = size;
        this.texture = new AtlasTexture();
        this.glyphs = new HashMap<>();
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

        if (!ensureFontLoaded()) {
            return TextBounds.EMPTY;
        }

        ensureGlyphs(string);
        return font.getSize(glyphs, string);
    }

    public void drawText(Matrix4f matrix, StylizedText text, int x, int y) {
        if (text.length() == 0) {
            return;
        }

        if (!ensureFontLoaded()) {
            return;
        }

        for (StylizedTextChunk chunk : text.chunks) {
            ensureGlyphs(chunk.text());
        }

        TextureColor2dRenderer renderer = RenderUtilities.instance.getTextureColor2dRenderer();
        renderer.begin();

        for (StylizedTextChunk chunk : text.chunks) {
            String string = chunk.text();
            int color = chunk.getColor();
            float r = (float) (color >> 16 & 0xFF) / 255;
            float g = (float) (color >> 8 & 0xFF) / 255;
            float b = (float) (color & 0xFF) / 255;

            x = font.render(renderer, glyphs, string, x, y, r, g, b, 1);
        }

        renderer.end(matrix, texture.getId());
    }

    public void drawText(Matrix4f matrix, String string, int x, int y, int color) {
        if (string == null) {
            return;
        }

        if (!ensureFontLoaded()) {
            return;
        }

        ensureGlyphs(string);

        float r = (float) (color >> 16 & 0xFF) / 255;
        float g = (float) (color >> 8 & 0xFF) / 255;
        float b = (float) (color & 0xFF) / 255;

        TextureColor2dRenderer renderer = RenderUtilities.instance.getTextureColor2dRenderer();
        renderer.begin();
        font.render(renderer, glyphs, string, x, y, r, g, b, 1);
        renderer.end(matrix, texture.getId());
    }

    public void dispose() {
        texture.dispose();
    }

    private boolean ensureFontLoaded() {
        if (font == null) {
            font = FontLibrary.instance.get(name);
            return font != null;
        }

        return true;
    }

    private void ensureGlyphs(String string) {
        assert font != null;

        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (!glyphs.containsKey(ch)) {
                glyphs.put(ch, font.createGlyph(texture, ch, size));
            }
        }
    }
}