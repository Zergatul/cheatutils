package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.font.GlyphFontRenderer;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.font.TextBounds;
import com.zergatul.cheatutils.render.MainFrameBuffer;

public class TextElement implements Element {

    private final GlyphFontRenderer font;
    private final StylizedText text;

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
        measuredHeight = font.getLineHeight();
    }

    @Override
    public void layout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(RenderingContext context) {
        MainFrameBuffer.enter();
        font.drawText(context.getMatrix(), text, x, y + measuredHeight - bounds.getY1()); // align bottom
    }
}