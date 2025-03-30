package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.AtlasTexture;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GlyphFontRenderer {

    private final Font font;
    private final boolean antiAliasing;
    public final AtlasTexture texture;
    private final Map<Character, Glyph> glyphs;
    private final StringBuilder builder;

    public GlyphFontRenderer(Font font, boolean antiAliasing) {
        this.font = font;
        this.antiAliasing = antiAliasing;
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
            return new TextBounds(0, 0, 0, 0);
        }

        ensureGlyphs(string);
        int width = 0;
        int height = 0;
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MAX_VALUE;
        for (int i = 0; i < string.length(); i++) {
            Glyph glyph = glyphs.get(string.charAt(i));
            width += glyph.getWidth();
            if (glyph.getHeight() > height) {
                height = glyph.getHeight();
            }
            if (glyph.getTop() < top) {
                top = glyph.getTop();
            }
            if (glyph.getBottom() < bottom) {
                bottom = glyph.getBottom();
            }
        }

        return new TextBounds(width, height, top, bottom);
    }

    public void drawText(Matrix4f matrix, StylizedText text, int x, int y) {
        if (text.length() == 0) {
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

            for (int i = 0; i < string.length(); i++) {
                Glyph glyph = glyphs.get(string.charAt(i));
                int width = glyph.getWidth();
                int height = glyph.getHeight();

                renderer.rect(x, y, width, height, glyph.getSprite(), r, g, b, 1);
                x += width;
            }
        }

        renderer.end(matrix, texture.getId());
    }

    public void drawText(Matrix4f matrix, String string, float x, float y, int color) {
        if (string == null) {
            return;
        }

        ensureGlyphs(string);

        float r = (float) (color >> 16 & 0xFF) / 255;
        float g = (float) (color >> 8 & 0xFF) / 255;
        float b = (float) (color & 0xFF) / 255;

        TextureColor2dRenderer renderer = RenderUtilities.instance.getTextureColor2dRenderer();
        renderer.begin();

        for (int i = 0; i < string.length(); i++) {
            Glyph glyph = glyphs.get(string.charAt(i));
            float width = glyph.getWidth();
            float height = glyph.getHeight();

            renderer.rect(x, y, width, height, glyph.getSprite(), r, g, b, 1);

            x += width;
        }

        renderer.end(matrix, texture.getId());
    }

    public void dispose() {
        texture.dispose();
    }

    private void ensureGlyphs(String string) {
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (!glyphs.containsKey(ch)) {
                glyphs.put(ch, Glyph.create(texture, font, ch, antiAliasing));
            }
        }
    }
}