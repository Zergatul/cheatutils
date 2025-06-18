package com.zergatul.cheatutils.common.events;

import net.minecraft.world.inventory.Slot;

public class ContainerScreenCalculateHoveredSlotEvent implements CancelableEvent {

    private boolean canceled;
    private Slot slot;

    public Slot getSlot() {
        return slot;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
        cancel();
    }
}
