package com.zergatul.cheatutils.mixins.forge;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    /*@Redirect(
            method = "keyPress(JIIII)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", opcode = Opcodes.GETFIELD, ordinal = 4))
    private Screen onCheckScreenPassEvents(Minecraft mc) {
        return InvMove.instance.overrideGetScreen(mc);
    }*/
}