package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.CraftingScreenMixinInterface;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingScreen.class)
public abstract class MixinCraftingScreen implements CraftingScreenMixinInterface {

    @Shadow
    protected abstract void slotClicked(Slot p_98469_, int p_98470_, int p_98471_, ClickType p_98472_);

    public void triggerSlotClicked(Slot slot, int p_98470_, int p_98471_, ClickType clickType) {
        this.slotClicked(slot, p_98470_, p_98471_, clickType);
    }
}