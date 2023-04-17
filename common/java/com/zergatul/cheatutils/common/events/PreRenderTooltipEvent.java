package com.zergatul.cheatutils.common.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public class PreRenderTooltipEvent implements CancelableEvent {

    private final PoseStack poseStack;
    private final ItemStack itemStack;
    private final int x;
    private final int y;
    private boolean canceled;

    public PreRenderTooltipEvent(PoseStack poseStack, ItemStack itemStack, int x, int y) {
        this.poseStack = poseStack;
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public PoseStack getPoseStack() {
        return poseStack;
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
