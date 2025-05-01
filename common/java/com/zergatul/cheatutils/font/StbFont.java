package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.TextureColor2dRenderer;
import com.zergatul.cheatutils.render.gl.AtlasTexture;
import com.zergatul.cheatutils.render.gl.images.ImageSource;
import com.zergatul.cheatutils.utils.FloatListHelper;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class StbFont extends Font {

    private final STBTTFontinfo info;
    @SuppressWarnings("unused")
    private final ByteBuffer buffer;
    private final int ascent;
    private final int descent;
    private final int lineGap;

    public StbFont(STBTTFontinfo info, ByteBuffer buffer) {
        this.info = info;
        this.buffer = buffer;

        IntBuffer ascent  = BufferUtils.createIntBuffer(1);
        IntBuffer descent = BufferUtils.createIntBuffer(1);
        IntBuffer lineGap = BufferUtils.createIntBuffer(1);
        STBTruetype.stbtt_GetFontVMetrics(info, ascent, descent, lineGap);
        this.ascent = ascent.get(0);
        this.descent = descent.get(0);
        this.lineGap = lineGap.get(0);
    }

    public void close() {
        info.close();
    }

    @Override
    public Glyph createGlyph(AtlasTexture texture, char ch, float size) {
        float scale = getScaleForPixelHeight(size);
        IntBuffer x0b = BufferUtils.createIntBuffer(1);
        IntBuffer y0b = BufferUtils.createIntBuffer(1);
        IntBuffer x1b = BufferUtils.createIntBuffer(1);
        IntBuffer y1b = BufferUtils.createIntBuffer(1);
        STBTruetype.stbtt_GetCodepointBitmapBox(info, ch, scale, scale, x0b, y0b, x1b, y1b);
        int x0 = x0b.get(0);
        int x1 = x1b.get(0);
        int y0 = y0b.get(0);
        int y1 = y1b.get(0);
        int width = x1 - x0;
        int height = y1 - y0;

        IntBuffer advb = BufferUtils.createIntBuffer(1);
        IntBuffer lsbb = BufferUtils.createIntBuffer(1);
        STBTruetype.stbtt_GetCodepointHMetrics(info, ch, advb, lsbb);
        int advance = Math.round(advb.get(0) * scale);
        int leftSideBearing = Math.round(lsbb.get(0) * scale);

        AtlasTexture.Item sprite = null;
        if (width > 0) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height);
            STBTruetype.stbtt_MakeCodepointBitmap(
                    info, buffer, width, height,
                    width, scale, scale, ch);
            sprite = texture.add(ImageSource.fromGrayscale(buffer, width, height));
        }

        return new Glyph(x0, x1, y0, y1, advance, leftSideBearing, sprite);
    }

    @Override
    public TextBounds getSize(Int2ObjectMap<Glyph> glyphs, String text) {
        int width = 0;
        int y0 = 0;
        int y1 = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            Glyph glyph = glyphs.get(ch);
            width += glyph.getLeftSideBearing();
            // for last character we don't use advance width, and just use glyph width
            if (i < text.length() - 1) {
                width += glyph.getAdvanceWidth();
            } else {
                width += glyph.getX0() + glyph.getWidth();
            }
            if (glyph.getY0() < y0) {
                y0 = glyph.getY0();
            }
            if (glyph.getY1() > y1) {
                y1 = glyph.getY1();
            }
        }
        return new TextBounds(width, y0, y1);
    }

    @Override
    public int render(TextureColor2dRenderer renderer, Int2ObjectMap<Glyph> glyphs, String text, int x, int y, float r, float g, float b, float a) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            Glyph glyph = glyphs.get(ch);
            x += glyph.getLeftSideBearing();
            if (!glyph.isBlank()) {
                /*if (!isMonospace && i > 0) {
                    int advance = STBTruetype.stbtt_GetCodepointKernAdvance(info, text.charAt(i - 1), ch);
                    x += Math.round(advance * scale);
                }*/
                float width = glyph.getWidth();
                float height = glyph.getHeight();
                renderer.rect(x + glyph.getX0(), y + glyph.getY0(), width, height, glyph.getSprite(), r, g, b, a);
            }
            x += glyph.getAdvanceWidth();
        }

        return x;
    }

    @Override
    public int render(FloatList buffer, Int2ObjectMap<Glyph> glyphs, String text, int x, int y, float r, float g, float b, float a) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            Glyph glyph = glyphs.get(ch);
            x += glyph.getLeftSideBearing();
            if (!glyph.isBlank()) {
                /*if (!isMonospace && i > 0) {
                    int advance = STBTruetype.stbtt_GetCodepointKernAdvance(info, text.charAt(i - 1), ch);
                    x += Math.round(advance * scale);
                }*/
                float width = glyph.getWidth();
                float height = glyph.getHeight();
                FloatListHelper.rect(
                        buffer,
                        x + glyph.getX0(), y + glyph.getY0(), width, height,
                        glyph.getSprite(), r, g, b, a);
            }
            x += glyph.getAdvanceWidth();
        }

        return x;
    }

    @Override
    public int getLineHeight(float size) {
        return Math.round(getScaleForPixelHeight(size) * (ascent - descent + lineGap));
    }

    private float getScaleForPixelHeight(float pixels) {
        return pixels / (ascent - descent);
    }
}