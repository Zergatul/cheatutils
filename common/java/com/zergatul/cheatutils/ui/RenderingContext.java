package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.mixins.common.accessors.GuiGraphicsAccessor;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.ScaledItemRenderState;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class RenderingContext {

    private final GuiGraphics graphics;
    private final Matrix4f matrix;
    private final int halfWidth;
    private final int halfHeight;
    private final int itemScale;
    private final int scale;
    private final Font font;
    private final RenderBuffers buffers;
    private final Runnable framebufferSetup;

    private List<ItemStackRenderEntry> itemStacksQueue;

    public RenderingContext(GuiGraphics graphics, Matrix4f matrix, int halfWidth, int halfHeight, int itemScale) {
        this(graphics, matrix, halfWidth, halfHeight, itemScale, MainFrameBuffer::bind);
    }

    public RenderingContext(GuiGraphics graphics, Matrix4f matrix, int halfWidth, int halfHeight, int itemScale, Runnable framebufferSetup) {
        this.graphics = graphics;
        this.matrix = matrix;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;

        Minecraft mc = Minecraft.getInstance();
        this.itemScale = itemScale == 0 ? mc.getWindow().getGuiScale() : itemScale;
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

    public int getItemScale() {
        return itemScale;
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
                graphics.pose().scale((float) itemScale / scale);
                if (itemScale != scale) {
                    renderScaledItem(entry);
                } else if (entry.entity != null) {
                    graphics.renderItem(entry.entity, entry.itemStack, 0, 0, 0);
                } else {
                    graphics.renderFakeItem(entry.itemStack, 0, 0, 0);
                }
                graphics.renderItemDecorations(font, entry.itemStack, 0, 0);
            }
            graphics.pose().popMatrix();
        }
    }

    private void renderScaledItem(ItemStackRenderEntry entry) {
        if (entry.itemStack.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        TrackingItemStackRenderState itemState = new TrackingItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(
                itemState, entry.itemStack, ItemDisplayContext.GUI,
                entry.entity != null ? entry.entity.level() : mc.level, entry.entity, 0);
        GuiItemRenderState guiItemState = new GuiItemRenderState(
                entry.itemStack.getItem().getName().toString(),
                new Matrix3x2f(graphics.pose()),
                itemState,
                0, 0,
                ((GuiGraphicsAccessor) graphics).getScissorStack_CU().peek());

        // Vanilla's item atlas uses GUI scale, regardless of the pose's scale.
        // Render the model to a texture at itemScale instead of stretching that atlas.
        ((GuiGraphicsAccessor) graphics).getGuiRenderState_CU().submitPicturesInPictureState(new ScaledItemRenderState(guiItemState, itemScale));
    }

    private void reset() {
        if (itemStacksQueue != null) {
            itemStacksQueue.clear();
        }
    }

    private record ItemStackRenderEntry(LivingEntity entity, ItemStack itemStack, int x, int y) {}
}