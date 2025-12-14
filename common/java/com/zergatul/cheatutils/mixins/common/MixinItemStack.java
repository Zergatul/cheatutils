package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.collections.TaggedArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(at = @At("RETURN"), method = "getTooltipLines", cancellable = true)
    private void onGetTooltipLines(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> info) {
        info.setReturnValue(new TaggedArrayList<>(info.getReturnValue(), (ItemStack) (Object) this));
    }
}