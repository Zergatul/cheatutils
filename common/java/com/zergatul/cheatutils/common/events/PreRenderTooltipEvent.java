package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class PreRenderTooltipEvent implements CancelableEvent {

    private final GuiGraphicsExtractor graphics;
    private final ItemStack itemStack;
    private final int x;
    private final int y;
    private boolean canceled;

    public PreRenderTooltipEvent(GuiGraphicsExtractor graphics, ItemStack itemStack, int x, int y) {
        this.graphics = graphics;
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public GuiGraphicsExtractor getGraphics() {
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