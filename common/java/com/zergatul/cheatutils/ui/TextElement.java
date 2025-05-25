package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.font.FontRenderer;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.font.TextBounds;
import it.unimi.dsi.fastutil.floats.FloatList;

public class TextElement implements Element {

    private final FontRenderer font;
    private final StylizedText text;

    private boolean compactHeight;
    private TextBounds bounds;
    private int measuredWidth, measuredHeight;
    private int x, y;

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
        if (font.isSingleTexture()) {
            FloatList buffer = context.getTextureColor2dBuffer(font.getTexture().getId());
            font.drawText(buffer, text, x, y + measuredHeight - bounds.getY1()); // align bottom
        } else {
            font.drawText(context.getMatrix(), text, x, y + measuredHeight - bounds.getY1());
        }
    }

    public TextElement setCompactHeight(boolean compact) {
        this.compactHeight = compact;
        return this;
    }
}