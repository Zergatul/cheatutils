package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    @Redirect(
            method = "keyPress(JIIII)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/Screen;passEvents:Z", opcode = Opcodes.GETFIELD))
    private boolean onCheckScreenPassEvents(Screen screen) {
        if (InvMove.instance.shouldPassEvents(screen)) {
            return true;
        } else {
            return screen.passEvents;
        }
    }
}