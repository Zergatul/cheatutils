package com.zergatul.cheatutils.mixins.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
        if (handle == this.minecraft.getWindow().handle() && action != 0) {
            InputConstants.Key key = InputConstants.getKey(event);
            Options options = this.minecraft.options;
            if (options.keyDebugModifier.matches(event)) {
                return;
            }
            if (this.minecraft.gui.screen() != null && InvMove.instance.shouldPassEvents(this.minecraft.gui.screen())) {
                KeyMapping.set(key, true);
                KeyMapping.click(key);
            }
        }
    }
}