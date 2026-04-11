package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.render.Position2dTextureColorRenderer;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.util.Mth;

import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public abstract class GlyphFontRenderer extends FontRenderer {

    private final GlyphRenderer glyphRenderer;
    private final FontRenderDetails details;

    protected GlyphFontRenderer(GlyphRenderer glyphRenderer, FontRenderDetails details) {
        assert RenderSystem.isOnRenderThread();

        this.glyphRenderer = glyphRenderer;
        this.details = details;
    }

    @Override
    public boolean uses(FontBackend backend) {
        return glyphRenderer == backend;
    }

    @Override
    public TextBounds getTextSize(StylizedText text) {
        glyphRenderer.markUse();

        for (StylizedTextChunk chunk : text.chunks) {
            glyphRenderer.ensureGlyphs(chunk.text());
        }
        return getTextSize(text.chars());
    }

    @Override
    public float getLineHeight() {
        return glyphRenderer.getLineHeight() + details.lineSpacing();
    }

    @Override
    public void drawText(RenderBuffers buffers, StylizedText text, float x, float y) {
        glyphRenderer.markUse();

        if (text == null) {
            return;
        }

        for (StylizedTextChunk chunk : text.chunks) {
            glyphRenderer.ensureGlyphs(chunk.text());
        }

        for (StylizedTextChunk chunk : text.chunks) {
            String string = chunk.text();
            int color = chunk.color() | 0xFF000000;
            x = renderGlyphs(buffers, string, x, y, color);
        }
    }

    private TextBounds getTextSize(IntStream chars) {
        float width = 0;
        float y0 = 0;
        float y1 = 0;
        Glyph glyph = null;
        PrimitiveIterator.OfInt iterator = chars.iterator();
        while (iterator.hasNext()) {
            char ch = (char) iterator.nextInt();
            glyph = glyphRenderer.get(ch);
            width += glyph.getLeftSideBearing();
            width += glyph.getAdvanceWidth();
            width += details.letterSpacing();
            if (glyph.getY0() < y0) {
                y0 = glyph.getY0();
            }
            if (glyph.getY1() > y1) {
                y1 = glyph.getY1();
            }
        }

        if (glyph != null) {
            width -= details.letterSpacing();
        }

        return new TextBounds(Mth.ceil(width), Math.round(y0), Math.round(y1));
    }

    private float renderGlyphs(RenderBuffers buffers, String text, float x, float y, int color) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            Glyph glyph = glyphRenderer.get(ch);
            x += glyph.getLeftSideBearing();
            if (!glyph.isBlank()) {
                if (details.dropShadow()) {
                    Position2dTextureColorRenderer.BufferBuilder buffer = buffers.getTexColor2d(RenderBuffers.FONT_SHADOW, glyphRenderer.texture.getTextureView());
                    buffer.rect(
                            Math.round(x + glyph.getX0()) + details.shadowOffsetX(),
                            Math.round(y + glyph.getY0()) + details.shadowOffsetY(),
                            (int) Math.ceil(glyph.getWidth()),
                            (int) Math.ceil(glyph.getHeight()),
                            glyph.getSprite(), ColorUtils.shadowed(color, SHADOW_FACTOR));
                }

                Position2dTextureColorRenderer.BufferBuilder buffer = buffers.getTexColor2d(RenderBuffers.FONT, glyphRenderer.texture.getTextureView());
                buffer.rect(
                        Math.round(x + glyph.getX0()),
                        Math.round(y + glyph.getY0()),
                        (int) Math.ceil(glyph.getWidth()),
                        (int) Math.ceil(glyph.getHeight()),
                        glyph.getSprite(), color);
            }
            x += glyph.getAdvanceWidth() + details.letterSpacing();
        }
        return x;
    }
}