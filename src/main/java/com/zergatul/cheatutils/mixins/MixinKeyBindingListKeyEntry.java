package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class MixinKeyBindingListKeyEntry {

    @ModifyArg(
            method = "Lnet/minecraft/client/gui/screens/controls/KeyBindsList$KeyEntry;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIIIIZF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;draw(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I", ordinal = 0),
            index = 1
    )
    private Component onRenderKeyName(Component component) {
        if (component  instanceof TranslatableComponent translatable) {
            final String prefix = "key.zergatul.cheatutils.reserved";
            if (translatable.getKey().startsWith(prefix)) {
                int index = Integer.parseInt(translatable.getKey().substring(prefix.length()));
                String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[index];
                if (name != null) {
                    return new TextComponent(index + ": " + name);
                }
            }
        }
        return component;
    }
}