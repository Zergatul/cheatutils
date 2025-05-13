package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.gl.AtlasTexture;
import com.zergatul.cheatutils.render.gl.images.ImageSource;
import net.minecraft.util.Mth;

import java.awt.*;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class AwtGlyphRenderer extends GlyphRenderer {

    private final Font font;
    private final boolean antiAliasing;

    public AwtGlyphRenderer(Font font, FontRenderParameters parameters) {
        this.font = font.deriveFont(parameters.size());
        this.antiAliasing = parameters.antiAliasing();
    }

    @Override
    public Glyph create(AtlasTexture texture, char ch) {
        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext context = new FontRenderContext(affineTransform, antiAliasing, true);
        String str = Character.toString(ch);
        Rectangle2D rect = font.getStringBounds(str, context);
        //LineMetrics metrics = font.getLineMetrics(str, context);

        int width = Mth.ceil(rect.getWidth());
        int height = Mth.ceil(rect.getHeight());
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setFont(font);
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antiAliasing ? RenderingHints.VALUE_ANTIALIAS_OFF : RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                antiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.drawString(str, 0, Mth.ceil(Math.abs(rect.getMinY())));
        graphics.dispose();

        var metrics = graphics.getFontMetrics();

        AtlasTexture.Item sprite = texture.add(ImageSource.fromBuffered(image));
        // TODO: check double values???
        return new Glyph(
                Mth.floor(rect.getMinX()),
                Mth.floor(rect.getMinY()),
                Mth.ceil(rect.getMaxX()),
                Mth.ceil(rect.getMaxY()),
                1,
                0,
                sprite);
    }
}