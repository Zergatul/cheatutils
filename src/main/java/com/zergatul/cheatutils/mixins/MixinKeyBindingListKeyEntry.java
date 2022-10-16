package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(KeyBindingList.KeyEntry.class)
public abstract class MixinKeyBindingListKeyEntry {

    @ModifyArg(
            method = "Lnet/minecraft/client/gui/widget/list/KeyBindingList$KeyEntry;render(Lcom/mojang/blaze3d/matrix/MatrixStack;IIIIIIIZF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;draw(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/text/ITextComponent;FFI)I", ordinal = 0),
            index = 1
    )
    private ITextComponent onRenderKeyName(ITextComponent component) {
        if (component instanceof TranslationTextComponent) {
            TranslationTextComponent translatable = (TranslationTextComponent) component;
            final String prefix = "key.zergatul.cheatutils.reserved";
            if (translatable.getKey().startsWith(prefix)) {
                int index = Integer.parseInt(translatable.getKey().substring(prefix.length()));
                String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[index];
                if (name != null) {
                    return new StringTextComponent(index + ": " + name);
                }
            }
        }
        return component;
    }
}