package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class PostRenderTooltipEvent {

    private final GuiGraphics graphics;
    private final ItemStack itemStack;
    private final int x;
    private final int y;

    public PostRenderTooltipEvent(GuiGraphics graphics, ItemStack itemStack, int x, int y) {
        this.graphics = graphics;
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}