package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegionFileStorage.class)
public interface RegionalFileStorageAccessor {

    @Invoker("write")
    void write_CU(ChunkPos pos, @Nullable CompoundTag value);
}