package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;releaseAll()V"))
    private void onSetScreenBeforeReleasingAllKeys(Screen screen, CallbackInfo info) {
        InvMove.instance.onOpenScreenStoreKeys(screen);
    }

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;releaseAll()V", shift = At.Shift.AFTER))
    private void onSetScreenAfterReleasingAllKeys(Screen screen, CallbackInfo info) {
        InvMove.instance.onOpenScreenRestoreKeys(screen);
    }
}