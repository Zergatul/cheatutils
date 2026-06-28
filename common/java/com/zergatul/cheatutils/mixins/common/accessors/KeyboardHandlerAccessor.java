package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyboardHandler.class)
public interface KeyboardHandlerAccessor {

    @Invoker("keyPress")
    void keyPress_CU(long handle, int action, KeyEvent event);

    @Invoker("charTyped")
    void charTyped_CU(long handle, CharacterEvent event);
}