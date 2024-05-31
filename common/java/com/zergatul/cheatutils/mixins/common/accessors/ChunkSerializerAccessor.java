package com.zergatul.cheatutils.mixins.common.accessors;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkSerializer.class)
public interface ChunkSerializerAccessor {

    @Accessor("BLOCK_STATE_CODEC")
    static Codec<PalettedContainer<BlockState>> getBlockStateCodec_CU() {
        throw new AssertionError();
    }
}