package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import com.zergatul.cheatutils.render.buffers.TextureColor2dRenderBuffer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
            int color = chunk.color();
            float r = (float) (color >> 16 & 0xFF) / 255;
            float g = (float) (color >> 8 & 0xFF) / 255;
            float b = (float) (color & 0xFF) / 255;

            x = renderGlyphs(buffers, string, x, y, r, g, b, 1);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, StylizedText text, float x, float y) {
        glyphRenderer.markUse();

        if (text == null) {
            return;
        }

        for (StylizedTextChunk chunk : text.chunks) {
            glyphRenderer.ensureGlyphs(chunk.text());
        }

        for (StylizedTextChunk chunk : text.chunks) {
            String string = chunk.text();
            int color = chunk.color();
            float r = (float) (color >> 16 & 0xFF) / 255;
            float g = (float) (color >> 8 & 0xFF) / 255;
            float b = (float) (color & 0xFF) / 255;

            x = renderGlyphs(buffers, string, x, y, r, g, b, 1);
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

    private float renderGlyphs(RenderBuffers buffers, String text, float x, float y, float r, float g, float b, float a) {
//        TextureColor2dRenderBuffer buffer = buffers.getTexColor2d(glyphRenderer.texture.getId());
//        for (int i = 0; i < text.length(); i++) {
//            char ch = text.charAt(i);
//            Glyph glyph = glyphRenderer.get(ch);
//            x += glyph.getLeftSideBearing();
//            if (!glyph.isBlank()) {
//                if (details.dropShadow()) {
//                    buffer.rect(
//                            Math.round(x + glyph.getX0()) + details.shadowOffsetX(),
//                            Math.round(y + glyph.getY0()) + details.shadowOffsetY(),
//                            (int) Math.ceil(glyph.getWidth()),
//                            (int) Math.ceil(glyph.getHeight()),
//                            glyph.getSprite(), r * SHADOW_FACTOR, g * SHADOW_FACTOR, b * SHADOW_FACTOR, a);
//                }
//                buffer.rect(
//                        Math.round(x + glyph.getX0()),
//                        Math.round(y + glyph.getY0()),
//                        (int) Math.ceil(glyph.getWidth()),
//                        (int) Math.ceil(glyph.getHeight()),
//                        glyph.getSprite(), r, g, b, a);
//            }
//            x += glyph.getAdvanceWidth() + details.letterSpacing();
//        }
//        return x;
        throw new AssertionError();
    }

    private float extractGlyphsState() {

    }
}