package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ContainerClickEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenu {

    @Inject(at = @At("HEAD"), method = "clicked")
    private void onClicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo info) {
        if (player instanceof LocalPlayer) {
            Events.ContainerMenuClick.trigger(new ContainerClickEvent(slotIndex, buttonNum, containerInput));
        }
    }
}