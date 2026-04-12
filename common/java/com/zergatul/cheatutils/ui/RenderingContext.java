package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.extensions.GuiGraphicsExtractorExtension;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix4f;

public class RenderingContext {

    private final int itemScale;
    private final RenderBuffers buffers;
    private final Runnable framebufferSetup;

    public RenderingContext(Matrix4f matrix, int itemScale) {
        this(matrix, itemScale, MainFrameBuffer::bind);
    }

    public RenderingContext(Matrix4f matrix, int itemScale, Runnable framebufferSetup) {
        this.itemScale = itemScale;
        this.buffers = new RenderBuffers(matrix);
        this.framebufferSetup = framebufferSetup;
    }

    public RenderBuffers getBuffers() {
        return buffers;
    }

    public int getItemScale() {
        return itemScale;
    }

    public void render(Element element, int x, int y, HorizontalAlign hAlign, VerticalAlign vAlign) {
        extractToBuffers(element, x, y, hAlign, vAlign);
        buffers.render(framebufferSetup);
    }

    private void extractToBuffers(Element element, int x, int y, HorizontalAlign hAlign, VerticalAlign vAlign) {
        element.measure(this);

        int width = element.getMeasuredWidth();
        int height = element.getMeasuredHeight();

        switch (hAlign) {
            case LEFT -> {}
            case CENTER -> x -= width / 2;
            case RIGHT -> x -= width;
        }

        switch (vAlign) {
            case TOP -> {}
            case MIDDLE -> y -= height / 2;
            case BOTTOM -> y -= height;
        }

        element.layout(x, y, width, height);
        element.render(this);
    }
}