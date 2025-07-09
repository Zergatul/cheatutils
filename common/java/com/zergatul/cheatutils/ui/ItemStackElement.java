package com.zergatul.cheatutils.ui;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ItemStackElement implements Element {

    private final LivingEntity entity;
    private final ItemStack itemStack;
    private int measuredWidth, measuredHeight;
    private int x, y;

    public ItemStackElement(ItemStack itemStack) {
        this.entity = null;
        this.itemStack = itemStack;
    }

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
        context.queueItemStackRender(entity, itemStack, x, y);
    }
}