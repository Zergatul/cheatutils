package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Holder.Reference.class)
public interface HolderReferenceAccessor {

    @Invoker("bindValue")
    void bindValue_CU(Object object);
}