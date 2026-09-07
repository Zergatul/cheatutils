package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GuiKeyBindingList.KeyEntry.class)
public abstract class MixinGuiKeybindingListKeyEntry {

    @Shadow
    @Final
    private KeyBinding keybinding;

    @ModifyArg(
            method = "drawEntry",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"),
            index = 0)
    private String onModifyKeyEntryText(String text) {
        if (KeyBindings.CATEGORY.equals(this.keybinding.getKeyCategory())) {
            int index = Integer.parseInt(this.keybinding.getKeyDescription().substring(KeyBindings.KEY_PREFIX.length()));
            String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[index];
            if (name != null) {
                return index + ": " + name;
            }
        }

        return text;
    }
}