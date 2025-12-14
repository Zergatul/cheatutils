package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zergatul.cheatutils.collections.TaggedArrayList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @ModifyExpressionValue(
            method = "getTooltipLines",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private ArrayList<?> onStoreItemStackInTooltipLines(ArrayList<?> original) {
        return new TaggedArrayList<>(original, (ItemStack) (Object) this);
    }
}