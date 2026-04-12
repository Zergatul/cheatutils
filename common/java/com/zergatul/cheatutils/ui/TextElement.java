package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.extensions.GuiGraphicsExtractorExtension;
import com.zergatul.cheatutils.font.FontRenderer;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.font.TextBounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class TextElement implements Element {

    private final FontRenderer font;
    private final StylizedText text;

    private boolean compactHeight;
    private TextBounds bounds;
    private int measuredWidth, measuredHeight;
    private int x, y;
    private int bgColor;

    public TextElement(FontRenderer font, StylizedText text) {
        this.font = font;
        this.text = text;
    }

    @Override
    public int getMeasuredWidth() {
        return measuredWidth;
    }

    @Override
    public int getMeasuredHeight() {
        return measuredHeight;
    }

    @Override
    public void measure(RenderingContext context) {
        bounds = font.getTextSize(text);
        measuredWidth = bounds.getWidth();
        measuredHeight = compactHeight ? bounds.getHeight() : Math.round(font.getLineHeight());
    }

    @Override
    public void layout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(RenderingContext context) {
        if (compactHeight) {
            font.drawText(context.getBuffers(), text, x, y + measuredHeight - bounds.getY1()); // align bottom
        } else {
            font.drawText(context.getBuffers(), text, x, y + font.getLineHeight());
            if (bgColor != 0) {
                context.getBuffers().getColor2d().rect(x, y, measuredWidth, font.getLineHeight(), bgColor);
            }
        }
    }

    @Override
    public void extract(GuiGraphicsExtractor graphics) {
        ((GuiGraphicsExtractorExtension) graphics).
    }

    public TextElement setCompactHeight(boolean compact) {
        this.compactHeight = compact;
        return this;
    }

    public TextElement setBackgroundColor(int color) {
        this.bgColor = color;
        return this;
    }
}