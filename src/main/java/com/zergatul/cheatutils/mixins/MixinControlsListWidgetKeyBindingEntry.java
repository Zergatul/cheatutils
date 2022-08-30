package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public abstract class MixinControlsListWidgetKeyBindingEntry {
    @ModifyArg(
            method = "Lnet/minecraft/client/gui/screen/option/ControlsListWidget$KeyBindingEntry;render(Lnet/minecraft/client/util/math/MatrixStack;IIIIIIIZF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", ordinal = 0),
            index = 1
    )
    private Text onRenderKeyName(Text component) {
        if (component instanceof MutableText mutable) {
            if (mutable.getContent() instanceof TranslatableTextContent translatable) {
                final String prefix = "key.zergatul.cheatutils.reserved";
                if (translatable.getKey().startsWith(prefix)) {
                    int index = Integer.parseInt(translatable.getKey().substring(prefix.length()));
                    String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[index];
                    if (name != null) {
                        return MutableText.of(new LiteralTextContent(index + ": " + name));
                    }
                }
            }
        }
        return component;
    }
}