package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.render.Color2dRenderer;
import com.zergatul.cheatutils.render.buffers.Color2dRenderBuffer;

public class DivisionElement implements Element {

    private Element content;
    private int borderWidth;
    private int borderColor;
    private int bgColor;
    private int margin;
    private int measuredWidth, measuredHeight;
    private int x, y;

    @Override
    public void measure(RenderingContext context) {
        if (context != null) {
            content.measure(context);
            measuredWidth = 2 * borderWidth + 2 * margin + content.getMeasuredWidth();
            measuredHeight = 2 * borderWidth + 2 * margin + content.getMeasuredHeight();
        } else {
            measuredWidth = 2 * borderWidth + 2 * margin;
            measuredHeight = 2 * borderWidth + 2 * margin;
        }
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
    public void layout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;

        if (content != null) {
            content.layout(x + borderWidth + margin, y + borderWidth + margin, content.getMeasuredWidth(), content.getMeasuredHeight());
        }
    }

    @Override
    public void render(RenderingContext context) {
        if (borderWidth > 0) {
            Color2dRenderBuffer buffer = context.getBuffers().getColor2d();
            float a = (borderColor >> 24 & 255) / 255.0F;
            float r = (borderColor >> 16 & 255) / 255.0F;
            float g = (borderColor >> 8 & 255) / 255.0F;
            float b = (borderColor & 255) / 255.0F;
            buffer.rect(x, y, measuredWidth, borderWidth, r, g, b, a);
            buffer.rect(x, y + measuredHeight - borderWidth, measuredWidth, borderWidth, r, g, b, a);
            buffer.rect(x, y, borderWidth, measuredHeight, r, g, b, a);
            buffer.rect(x + measuredWidth - borderWidth, y, borderWidth, measuredHeight, r, g, b, a);
        }

        if (bgColor != 0) {
            Color2dRenderBuffer buffer = context.getBuffers().getColor2d();
            float a = (bgColor >> 24 & 255) / 255.0F;
            float r = (bgColor >> 16 & 255) / 255.0F;
            float g = (bgColor >> 8 & 255) / 255.0F;
            float b = (bgColor & 255) / 255.0F;
            buffer.rect(
                    x + borderWidth, y + borderWidth,
                    measuredWidth - 2 * borderWidth, measuredHeight - 2 * borderWidth,
                    r, g, b, a);
        }

        if (content != null) {
            content.render(context);
        }
    }

    public DivisionElement setContent(Element element) {
        this.content = element;
        return this;
    }

    public DivisionElement setBorderWidth(int width) {
        this.borderWidth = width;
        return this;
    }

    public DivisionElement setBorderColor(int color) {
        this.borderColor = color;
        return this;
    }

    public DivisionElement setBackgroundColor(int color) {
        this.bgColor = color;
        return this;
    }

    public DivisionElement setMargin(int width) {
        this.margin = width;
        return this;
    }
}