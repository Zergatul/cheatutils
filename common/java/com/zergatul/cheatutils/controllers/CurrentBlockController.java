package com.zergatul.cheatutils.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CurrentBlockController {

    public static final CurrentBlockController instance = new CurrentBlockController();

    private BlockPos pos;
    private BlockState state;

    private CurrentBlockController() {

    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public void clear() {
        pos = null;
        state = null;
    }

    public void set(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }
}