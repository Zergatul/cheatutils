package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(
            method = "keyPress",
            at = @At("TAIL"))
    private void onKeyPress(long handle, int action, KeyEvent event, CallbackInfo info) {
        if (handle == this.minecraft.getWindow().handle()) {
            InvMove.instance.onKeyPress(action, event);
        }
    }
}