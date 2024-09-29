package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(
            method = "keyPress",
            at = @At(value = "LOAD", ordinal = 1),
            ordinal = 0)
    private boolean[] onModifyScreenKeyState(boolean[] state) {
        if (InvMove.instance.shouldPassEvents(this.minecraft.screen)) {
            state[0] = false;
        }
        return state;
    }

    @ModifyVariable(
            method = "keyPress",
            at = @At(value = "LOAD", ordinal = 2),
            index = 10)
    private boolean onModifyNoScreen(boolean noScreen) {
        if (InvMove.instance.shouldPassEvents(this.minecraft.screen)) {
            return true;
        } else {
            return noScreen;
        }
    }
}