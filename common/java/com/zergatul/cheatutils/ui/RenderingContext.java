package com.zergatul.cheatutils.ui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

import java.util.Objects;

public class RenderingContext {

    private final int itemScale;
    private final RenderBuffers buffers;
    private final RenderTarget renderTarget;

    public RenderingContext(Matrix4f matrix, int itemScale) {
        this(matrix, itemScale, Minecraft.getInstance().gameRenderer.mainRenderTarget());
    }

    public RenderingContext(Matrix4f matrix, int itemScale, RenderTarget renderTarget) {
        this.itemScale = itemScale;
        this.buffers = new RenderBuffers(matrix);
        this.renderTarget = renderTarget;
    }

    public RenderBuffers getBuffers() {
        return buffers;
    }

    public int getItemScale() {
        return itemScale;
    }

    public void clearTarget() {
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(
                Objects.requireNonNull(renderTarget.getColorTexture()),
                0);
    }

    public void render(Element element, int x, int y, HorizontalAlign hAlign, VerticalAlign vAlign) {
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

        buffers.render(this.renderTarget);
    }
}