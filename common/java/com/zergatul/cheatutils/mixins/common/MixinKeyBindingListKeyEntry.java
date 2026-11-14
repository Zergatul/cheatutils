package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.modules.KeyBindingEntry;
import com.zergatul.cheatutils.modules.scripting.keybindings.KeyBindings;
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
            method = "extractContent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private Component onRenderKeyName(Component component) {
        if (component instanceof MutableComponent mutable) {
            if (mutable.getContents() instanceof TranslatableContents translatable) {
                final String prefix = "key.zergatul.cheatutils.reserved";
                if (translatable.getKey().startsWith(prefix)) {

                    int index = Integer.parseInt(translatable.getKey().substring(prefix.length()));
                    KeyBindingEntry entry = KeyBindings.INSTANCE.getSettings().get().entries().stream()
                            .filter(e -> e.keyIndex() == index)
                            .findFirst()
                            .orElse(null);
                    if (entry != null) {
                        return MutableComponent.create(new PlainTextContents.LiteralContents(index + ": " + entry.name()));
                    }
                }
            }
        }
        return component;
    }
}