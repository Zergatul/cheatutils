package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.collections.TaggedArrayList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {

    @Unique
    private TaggedArrayList<Component, ItemStack> cheatutils$storedComponents;

    @Inject(at = @At("HEAD"), method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V")
    private void onBeforeSetTooltipForNextFrame(
            Font font,
            List<Component> components,
            Optional<TooltipComponent> optional,
            int x, int y,
            @Nullable Identifier location,
            CallbackInfo info
    ) {
        if (components instanceof TaggedArrayList<?,?>) {
            cheatutils$storedComponents = (TaggedArrayList<Component, ItemStack>) components;
        }
    }

    @ModifyArg(
            method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Z)V"))
    private List<ClientTooltipComponent> onModifyComponentsList(List<ClientTooltipComponent> components) {
        if (cheatutils$storedComponents != null) {
            return new TaggedArrayList<>(components, cheatutils$storedComponents.getTag());
        } else {
            return components;
        }
    }

    @Inject(at = @At("TAIL"), method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V")
    private void onAfterSetTooltipForNextFrame(
            Font font,
            List<Component> components,
            Optional<TooltipComponent> optional,
            int x, int y,
            @Nullable Identifier location,
            CallbackInfo info
    ) {
        cheatutils$storedComponents = null;
    }
}