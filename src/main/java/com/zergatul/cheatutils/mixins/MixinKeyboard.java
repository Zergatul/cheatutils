package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard {

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/Keyboard;onKey(JIIII)V")
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
            ModApiWrapper.triggerOnKeyInput();
        }
    }
}