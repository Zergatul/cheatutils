package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.gl.AtlasTexture;
import com.zergatul.cheatutils.render.gl.images.ImageSource;

import java.awt.*;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class AwtGlyphRenderer extends GlyphRenderer {

    private final Font font;
    private final boolean antiAliasing;
    private final float lineHeight;

    public AwtGlyphRenderer(Font font, FontRenderParameters parameters) {
        this.font = font.deriveFont(parameters.size());
        this.antiAliasing = parameters.antiAliasing();
        this.lineHeight = calculateLineHeight();
    }

    @Override
    public FontRenderer createFontRenderer(FontRenderDetails details) {
        return new AwtFontRenderer(this, details);
    }

    @Override
    public float getLineHeight() {
        return lineHeight;
    }

    @Override
    protected Glyph renderGlyph(char ch) {
        FontRenderContext context = createFontRenderContext();
        String str = Character.toString(ch);

        GlyphVector vector = font.createGlyphVector(context, str);
        GlyphMetrics metrics = vector.getGlyphMetrics(0);
        Rectangle rect = vector.getGlyphPixelBounds(0, context, 0, 0);
        float leftSideBearing = metrics.getLSB();
        float advance = metrics.getAdvance();

        AtlasTexture.Item sprite;
        if (rect.width > 0 && rect.height > 0) {
            BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setFont(font);
            graphics.setColor(new Color(255, 255, 255, 0));
            graphics.fillRect(0, 0, rect.width, rect.height);
            graphics.setColor(Color.WHITE);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    antiAliasing ? RenderingHints.VALUE_ANTIALIAS_OFF : RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    antiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            graphics.drawString(str, -rect.x, -rect.y);
            graphics.dispose();

            sprite = texture.add(ImageSource.fromBuffered(image));
        } else {
            sprite = null;
        }

        return new Glyph(
                (float) rect.getX(),
                (float) rect.getY(),
                (float) rect.getWidth(),
                (float) rect.getHeight(),
                advance,
                leftSideBearing,
                sprite);
    }

    private float calculateLineHeight() {
        FontRenderContext context = createFontRenderContext();
        LineMetrics metrics = font.getLineMetrics("A", context);
        return metrics.getHeight();
    }

    private FontRenderContext createFontRenderContext() {
        AffineTransform affineTransform = new AffineTransform();
        return new FontRenderContext(affineTransform, antiAliasing, true);
    }
}