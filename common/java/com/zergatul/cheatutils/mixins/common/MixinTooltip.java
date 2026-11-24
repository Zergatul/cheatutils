package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zergatul.cheatutils.collections.TaggedArrayList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(Tooltip.class)
public abstract class MixinTooltip {

    @Shadow
    @Final
    private List<Component> messages;

    @ModifyArg(
            method = "createForItem(Ljava/util/List;Lnet/minecraft/network/chat/Component;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/gui/components/Tooltip;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Tooltip;<init>(Lnet/minecraft/network/chat/Component;Ljava/util/List;Lnet/minecraft/network/chat/Component;Ljava/util/Optional;Lnet/minecraft/resources/Identifier;ZZZ)V"),
            index = 1)
    private static List<Component> onStoreTooltipItem(List<Component> messages, @Local(argsOnly = true) ItemStack itemStack) {
        return new TaggedArrayList<>(messages, itemStack);
    }

    @ModifyExpressionValue(method = "toCharSequence", at = @At(value = "NEW", target = "()Ljava/util/ArrayList;"))
    private ArrayList<FormattedCharSequence> onCreateTooltipLines(ArrayList<FormattedCharSequence> lines) {
        if (messages instanceof TaggedArrayList<?, ?> tagged && tagged.getTag() instanceof ItemStack itemStack) {
            return new TaggedArrayList<>(lines, itemStack);
        }
        return lines;
    }
}