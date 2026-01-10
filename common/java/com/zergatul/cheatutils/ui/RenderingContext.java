package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class RenderingContext {

    private final GuiGraphics graphics;
    private final Matrix4f matrix;
    private final int halfWidth;
    private final int halfHeight;
    private final int scale;
    private final Font font;
    private final RenderBuffers buffers;
    private final Runnable framebufferSetup;

    private List<ItemStackRenderEntry> itemStacksQueue;

    public RenderingContext(GuiGraphics graphics, Matrix4f matrix, int halfWidth, int halfHeight) {
        this(graphics, matrix, halfWidth, halfHeight, MainFrameBuffer::bind);
    }

    public RenderingContext(GuiGraphics graphics, Matrix4f matrix, int halfWidth, int halfHeight, Runnable framebufferSetup) {
        this.graphics = graphics;
        this.matrix = matrix;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;

        Minecraft mc = Minecraft.getInstance();
        this.scale = mc.getWindow().getGuiScale();
        this.font = mc.font;
        this.buffers = new RenderBuffers();
        this.framebufferSetup = framebufferSetup;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public RenderBuffers getBuffers() {
        return buffers;
    }

    public Matrix4f getMatrix() {
        return matrix;
    }

    public int getScale() {
        return scale;
    }

    public void queueItemStackRender(LivingEntity entity, ItemStack itemStack, int x, int y) {
        if (itemStacksQueue == null) {
            itemStacksQueue = new ArrayList<>(6);
        }
        itemStacksQueue.add(new ItemStackRenderEntry(entity, itemStack, x, y));
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
        reset();
        element.render(this);

        buffers.render(matrix, framebufferSetup);

        if (itemStacksQueue != null) {
            graphics.pose().pushMatrix();
            for (ItemStackRenderEntry entry : itemStacksQueue) {
                graphics.pose().identity();
                graphics.pose().translate(1f * (entry.x + halfWidth) / scale, 1f * (entry.y + halfHeight) / scale);
                if (entry.entity != null) {
                    graphics.renderItem(entry.entity, entry.itemStack, 0, 0, 0);
                } else {
                    graphics.renderFakeItem(entry.itemStack, 0, 0,0);
                }
                graphics.renderItemDecorations(font, entry.itemStack, 0, 0);
            }
            graphics.pose().popMatrix();
        }
    }

    private void reset() {
        if (itemStacksQueue != null) {
            itemStacksQueue.clear();
        }
    }

    private record ItemStackRenderEntry(LivingEntity entity, ItemStack itemStack, int x, int y) {}
}