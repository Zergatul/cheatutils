package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.render.gl.GlStateTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ItemStackElement implements Element {

    private final LivingEntity entity;
    private final ItemStack itemStack;
    private int measuredWidth, measuredHeight;
    private int x, y;

    public ItemStackElement(LivingEntity entity, ItemStack itemStack) {
        this.entity = entity;
        this.itemStack = itemStack;
    }

    @Override
    public void measure(RenderingContext context) {
        this.measuredWidth = 16 * context.getScale();
        this.measuredHeight = 16 * context.getScale();
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
    }

    @Override
    public void render(RenderingContext context) {
        GlStateTracker.restore(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);

        GuiGraphics graphics = context.getGraphics();
        int scale = context.getScale();
        graphics.pose().pushPose();
        graphics.pose().setIdentity();
        graphics.pose().translate(1d * (x + context.getHalfWidth()) / scale, 1d * (y + context.getHalfHeight()) / scale, 0);
        graphics.renderItem(entity, itemStack, 0, 0, 0);
        graphics.renderItemDecorations(context.getFont(), itemStack, 0, 0);
        graphics.pose().popPose();

        GlStateTracker.save(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
    }
}