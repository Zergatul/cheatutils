package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GlyphFontRenderer {

    private final Font font;
    private final boolean antiAliasing;
    private final Map<Character, Glyph> glyphs;
    private final StringBuilder builder;

    public GlyphFontRenderer(Font font, boolean antiAliasing) {
        this.font = font;
        this.antiAliasing = antiAliasing;
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

    public void drawText(Matrix4f matrix, String string, float x, float y, int color) {
        if (string == null) {
            return;
        }

        ensureGlyphs(string);

        float r = (float) (color >> 16 & 0xFF) / 255;
        float g = (float) (color >> 8 & 0xFF) / 255;
        float b = (float) (color & 0xFF) / 255;

        TextureColor2dRenderer renderer = RenderUtilities.instance.getTextureColor2dRenderer();

        for (int i = 0; i < string.length(); i++) {
            Glyph glyph = glyphs.get(string.charAt(i));
            float width = glyph.getWidth();
            float height = glyph.getHeight();

            renderer.begin();
            renderer.rect(x, y, width, height, r, g, b, 1);
            renderer.end(matrix, glyph.getTextureId());

            x += width;
        }
    }

    public void dispose() {
        for (Glyph glyph : glyphs.values()) {
            glyph.dispose();
        }
    }

    private void ensureGlyphs(String string) {
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (!glyphs.containsKey(ch)) {
                glyphs.put(ch, new Glyph(font, ch, antiAliasing));
            }
        }
    }
}