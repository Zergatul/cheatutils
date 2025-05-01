package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.font.GlyphFontRenderer;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.font.TextBounds;
import it.unimi.dsi.fastutil.floats.FloatList;

public class TextElement implements Element {

    private final GlyphFontRenderer font;
    private final StylizedText text;

    private boolean compactHeight;
    private TextBounds bounds;
    private int measuredWidth, measuredHeight;
    private int x, y;

    public TextElement(GlyphFontRenderer font, StylizedText text) {
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
        measuredHeight = compactHeight ? bounds.getHeight() : font.getLineHeight();
    }

    @Override
    public void layout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(RenderingContext context) {
        FloatList buffer = context.getTextureColor2dBuffer(font.getTextureId());
        font.drawText(buffer, text, x, y + measuredHeight - bounds.getY1()); // align bottom
    }

    public TextElement setCompactHeight(boolean compact) {
        this.compactHeight = compact;
        return this;
    }
}