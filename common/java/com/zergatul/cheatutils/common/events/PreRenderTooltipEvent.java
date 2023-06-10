package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class PreRenderTooltipEvent implements CancelableEvent {

    private final GuiGraphics graphics;
    private final ItemStack itemStack;
    private final int x;
    private final int y;
    private boolean canceled;

    public PreRenderTooltipEvent(GuiGraphics graphics, ItemStack itemStack, int x, int y) {
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

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
}