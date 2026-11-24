package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zergatul.cheatutils.collections.TaggedArrayList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class MixinGuiGraphicsExtractor {

    @ModifyReturnValue(method = "createTooltipComponents", at = @At("RETURN"))
    private static List<ClientTooltipComponent> onCreateTooltipComponents(
            List<ClientTooltipComponent> components,
            @Local(argsOnly = true) List<Component> texts
    ) {
        return copyItemTag_CU(components, texts);
    }

    @ModifyArg(
            method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;IIZLnet/minecraft/resources/Identifier;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;ZI)V"),
            index = 1)
    private List<ClientTooltipComponent> onConvertItemTooltip(
            List<ClientTooltipComponent> components,
            @Local(argsOnly = true) List<?> tooltip
    ) {
        return copyItemTag_CU(components, tooltip);
    }

    @ModifyArg(
            method = {
                    "setComponentTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/resources/Identifier;)V",
                    "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/resources/Identifier;)V",
                    "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;IIZ)V"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Z)V"),
            index = 1)
    private List<ClientTooltipComponent> onConvertTooltipLines(
            List<ClientTooltipComponent> components,
            @Local(argsOnly = true) List<?> texts
    ) {
        return copyItemTag_CU(components, texts);
    }

    @Unique
    private static List<ClientTooltipComponent> copyItemTag_CU(List<ClientTooltipComponent> components, List<?> texts) {
        if (texts instanceof TaggedArrayList<?, ?> tagged && tagged.getTag() instanceof ItemStack itemStack) {
            return new TaggedArrayList<>(components, itemStack);
        }
        return components;
    }
}