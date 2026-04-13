package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.AtlasTexture;
import com.zergatul.cheatutils.render.images.ImageSource;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class StbGlyphRenderer extends GlyphRenderer {

    private final StbFont font;
    private final float scale;
    private final float lineHeight;

    protected StbGlyphRenderer(SystemFontInfo info, StbFont font, FontRenderParameters parameters) {
        super(String.format("%s[%s]", info.name, parameters.toString(FontRendererType.STB)));
        this.font = font;
        this.scale = font.getScaleForPixelHeight(parameters.size());
        this.lineHeight = font.getLineHeight(parameters.size());
    }

    @Override
    public FontRenderer createFontRenderer(FontRenderDetails details) {
        return new StbFontRenderer(this, details);
    }

    @Override
    public float getLineHeight() {
        return lineHeight;
    }

    @Override
    protected Glyph renderGlyph(char ch) {
        IntBuffer x0b = BufferUtils.createIntBuffer(1);
        IntBuffer y0b = BufferUtils.createIntBuffer(1);
        IntBuffer x1b = BufferUtils.createIntBuffer(1);
        IntBuffer y1b = BufferUtils.createIntBuffer(1);
        STBTruetype.stbtt_GetCodepointBitmapBox(font.getFontInfo(), ch, scale, scale, x0b, y0b, x1b, y1b);
        int x0 = x0b.get(0);
        int x1 = x1b.get(0);
        int y0 = y0b.get(0);
        int y1 = y1b.get(0);
        int width = x1 - x0;
        int height = y1 - y0;

        IntBuffer advb = BufferUtils.createIntBuffer(1);
        IntBuffer lsbb = BufferUtils.createIntBuffer(1);
        STBTruetype.stbtt_GetCodepointHMetrics(font.getFontInfo(), ch, advb, lsbb);
        float advance = advb.get(0) * scale;
        float leftSideBearing = lsbb.get(0) * scale;

        AtlasTexture.Item sprite = null;
        if (width > 0) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height);
            STBTruetype.stbtt_MakeCodepointBitmap(
                    font.getFontInfo(), buffer, width, height,
                    width, scale, scale, ch);
            sprite = texture.add(ImageSource.fromGrayscale(buffer, width, height));
        }

        return new Glyph(x0, y0, width, height, advance, leftSideBearing, sprite);
    }
}