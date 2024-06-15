package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class MixinKeyBindingListKeyEntry {

    @ModifyArg(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIIIIIZF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I", ordinal = 0),
            index = 1)
    private Component onRenderKeyName(Component component) {
        if (component instanceof MutableComponent mutable) {
            if (mutable.getContents() instanceof TranslatableContents translatable) {
                final String prefix = "key.zergatul.cheatutils.reserved";
                if (translatable.getKey().startsWith(prefix)) {
                    int index = Integer.parseInt(translatable.getKey().substring(prefix.length()));
                    String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[index];
                    if (name != null) {
                        return MutableComponent.create(new PlainTextContents.LiteralContents(index + ": " + name));
                    }
                }
            }
        }
        return component;
    }
}