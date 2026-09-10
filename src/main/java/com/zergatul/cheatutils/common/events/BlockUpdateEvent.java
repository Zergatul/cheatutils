package com.zergatul.cheatutils.common.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public final class BlockUpdateEvent {

    private final Chunk chunk;
    private final BlockPos pos;
    private final IBlockState state;

    public BlockUpdateEvent(Chunk chunk, BlockPos pos, IBlockState state) {
        this.chunk = chunk;
        this.pos = pos;
        this.state = state;
    }

    public Chunk chunk() {
        return chunk;
    }

    public BlockPos pos() {
        return pos;
    }

    public IBlockState state() {
        return state;
    }
}