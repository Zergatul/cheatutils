package com.zergatul.cheatutils.common.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public final class BlockUpdateEvent {

    private final BlockPos pos;
    private final IBlockState state;

    public BlockUpdateEvent(BlockPos pos, IBlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public BlockPos pos() {
        return pos;
    }

    public IBlockState state() {
        return state;
    }
}