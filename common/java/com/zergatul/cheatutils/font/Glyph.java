package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.gl.AtlasTexture;

import java.awt.image.BufferedImage;

public class Glyph {

    public static final Glyph EMPTY = new Glyph(0, 0, 0, 0, 0, 0,null);

    private final int x0;
    private final int x1;
    private final int y0;
    private final int y1;
    private final int advance;
    private final int leftSideBearing;
    private final AtlasTexture.Item sprite;

    public Glyph(int x0, int x1, int y0, int y1, int advance, int leftSideBearing, AtlasTexture.Item sprite) {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.advance = advance;
        this.leftSideBearing = leftSideBearing;
        this.sprite = sprite;
    }

    /*public static Glyph create(AtlasTexture texture, Font font, char ch, boolean antiAliasing) {
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

        AtlasTexture.Item sprite = texture.add(ImageSource.fromBuffered(image));
        return new Glyph(width, height, baseline, top, bottom, sprite);
    }*/

    public int getWidth() {
        return x1 - x0;
    }

    public int getHeight() {
        return y1 - y0;
    }

    public int getX0() {
        return x0;
    }

    public int getX1() {
        return x1;
    }

    public int getY0() {
        return y0;
    }

    public int getY1() {
        return y1;
    }

    public boolean isBlank() {
        return x0 == x1;
    }

    public int getAdvanceWidth() {
        return advance;
    }

    public int getLeftSideBearing() {
        return leftSideBearing;
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