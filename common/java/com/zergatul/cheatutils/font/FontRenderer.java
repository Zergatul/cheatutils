package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.AtlasTexture;
import com.zergatul.cheatutils.utils.FloatListHelper;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public abstract class FontRenderer implements GlyphRendererHolder {

    private final GlyphRenderer glyphRenderer;
    private final FontRenderDetails details;

    protected FontRenderer(GlyphRenderer glyphRenderer, FontRenderDetails details) {
        assert RenderSystem.isOnRenderThread();

        this.glyphRenderer = glyphRenderer;
        this.details = details;
    }

    @Override
    public boolean uses(GlyphRenderer renderer) {
        return glyphRenderer == renderer;
    }

    public AtlasTexture getTexture() {
        return glyphRenderer.texture;
    }

    public TextBounds getTextSize(StylizedText text) {
        for (StylizedTextChunk chunk : text.chunks) {
            glyphRenderer.ensureGlyphs(chunk.text());
        }
        return getTextSize(text.chars());
    }

    public TextBounds getTextSize(String text) {
        if (text == null || text.isEmpty()) {
            return TextBounds.EMPTY;
        }

        glyphRenderer.ensureGlyphs(text);
        return getTextSize(text.chars());
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

    public float getLineHeight() {
        return glyphRenderer.getLineHeight();
    }

    public void drawText(Matrix4f matrix, String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }

        glyphRenderer.ensureGlyphs(text);

        float r = (float) (color >> 16 & 0xFF) / 255;
        float g = (float) (color >> 8 & 0xFF) / 255;
        float b = (float) (color & 0xFF) / 255;

        TextureColor2dRenderer renderer = RenderUtilities.instance.getTextureColor2dRenderer();
        renderer.begin();
        renderGlyphs(renderer, text, x, y, r, g, b, 1);
        renderer.end(matrix, glyphRenderer.texture.getId());
    }

    public void drawText(FloatList buffer, StylizedText text, float x, float y) {
        if (text == null) {
            return;
        }

        for (StylizedTextChunk chunk : text.chunks) {
            glyphRenderer.ensureGlyphs(chunk.text());
        }

        for (StylizedTextChunk chunk : text.chunks) {
            String string = chunk.text();
            int color = chunk.getColor();
            float r = (float) (color >> 16 & 0xFF) / 255;
            float g = (float) (color >> 8 & 0xFF) / 255;
            float b = (float) (color & 0xFF) / 255;

            x = renderGlyphs(buffer, string, x, y, r, g, b, 1);
        }
    }

    private float renderGlyphs(TextureColor2dRenderer renderer, String text, float x, float y, float r, float g, float b, float a) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            Glyph glyph = glyphRenderer.get(ch);
            x += glyph.getLeftSideBearing();
            if (!glyph.isBlank()) {
                renderer.rect(
                        Math.round(x + glyph.getX0()),
                        Math.round(y + glyph.getY0()),
                        (int) Math.ceil(glyph.getWidth()),
                        (int) Math.ceil(glyph.getHeight()),
                        glyph.getSprite(),
                        r, g, b, a);
            }
            x += glyph.getAdvanceWidth() + details.letterSpacing();
        }
        return x;
    }

    private float renderGlyphs(FloatList buffer, String text, float x, float y, float r, float g, float b, float a) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            Glyph glyph = glyphRenderer.get(ch);
            x += glyph.getLeftSideBearing();
            if (!glyph.isBlank()) {
                FloatListHelper.rect(
                        buffer,
                        Math.round(x + glyph.getX0()),
                        Math.round(y + glyph.getY0()),
                        (int) Math.ceil(glyph.getWidth()),
                        (int) Math.ceil(glyph.getHeight()),
                        glyph.getSprite(), r, g, b, a);
            }
            x += glyph.getAdvanceWidth() + details.letterSpacing();
        }
        return x;
    }
}