package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    @Redirect(
            method = "keyPress(JIIII)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", opcode = Opcodes.GETFIELD, ordinal = 4))
    private Screen onCheckScreenPassEvents(Minecraft mc) {
        return InvMove.instance.overrideGetScreen(mc);
    }
}