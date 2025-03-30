package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.gl.AtlasTexture;
import net.minecraft.util.Mth;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Glyph {

    private final int width;
    private final int height;
    private final int baseline;
    private final int top;
    private final int bottom;
    private final AtlasTexture.Item sprite;

    private Glyph(int width, int height, int baseline, int top, int bottom, AtlasTexture.Item sprite) {
        this.width = width;
        this.height = height;
        this.baseline = baseline;
        this.top = top;
        this.bottom = bottom;
        this.sprite = sprite;
    }

    public static Glyph create(AtlasTexture texture, Font font, char ch, boolean antiAliasing) {
        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affineTransform, antiAliasing, false);
        String charStr = Character.toString(ch);
        Rectangle2D bounds = font.getStringBounds(charStr, frc);
        int height = Mth.ceil(bounds.getHeight());
        int width = Mth.ceil(bounds.getWidth());
        int baseline = Mth.ceil(Math.abs(bounds.getMinY()));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setFont(font);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(255, 255, 255, 255));
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antiAliasing ? RenderingHints.VALUE_ANTIALIAS_OFF : RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                antiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.drawString(charStr, 0, baseline);
        g.dispose();

        int shift = -1;
        for (int y = 0; y < height; y++) {
            if (hasPixels(image, y)) {
                shift = y;
                break;
            }
        }

        int top, bottom;
        if (shift == -1) {
            // blank glyph
            top = baseline;
            bottom = height - baseline;
        } else {
            top = shift;

            shift = -1;
            for (int y = height - 1; y >= 0; y--) {
                if (hasPixels(image, y)) {
                    shift = y;
                    break;
                }
            }

            bottom = height - 1 - shift;
        }

        AtlasTexture.Item sprite = texture.add(image);
        return new Glyph(width, height, baseline, top, bottom, sprite);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public int getBaseline() {
        return baseline;
    }

    public AtlasTexture.Item getSprite() {
        return sprite;
    }

    private static boolean hasPixels(BufferedImage image, int y) {
        for (int x = 0; x < image.getWidth(); x++) {
            if ((image.getRGB(x, y) & 0xFF000000) != 0) {
                return true;
            }
        }
        return false;
    }
}