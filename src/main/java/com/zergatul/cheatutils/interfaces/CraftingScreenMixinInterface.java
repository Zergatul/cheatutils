package com.zergatul.cheatutils.interfaces;

import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public interface CraftingScreenMixinInterface {
    void triggerSlotClicked(Slot slot, int p_98470_, int p_98471_, ClickType clickType);
}