package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {

    @Accessor("isDestroying")
    boolean getIsDestroying_CU();

    @Accessor("destroyBlockPos")
    BlockPos getDestroyBlockPos_CU();

    @Accessor("destroyProgress")
    float getDestroyProgress_CU();
}