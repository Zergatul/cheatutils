package com.zergatul.cheatutils.common.events;

import net.minecraft.client.gui.components.inventory.SlotWidget;

public class ContainerScreenCalculateHoveredSlotEvent implements CancelableEvent {

    private boolean canceled;
    private SlotWidget slot;

    public SlotWidget getSlot() {
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

    public void setSlot(SlotWidget slot) {
        this.slot = slot;
        cancel();
    }
}
