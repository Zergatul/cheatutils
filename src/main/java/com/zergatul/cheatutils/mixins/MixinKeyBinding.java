package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.KeyBindingMixinInterface;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding implements KeyBindingMixinInterface {

    @Shadow
    private InputUtil.Key boundKey;

    @Override
    public InputUtil.Key getBoundKey() {
        return this.boundKey;
    }
}