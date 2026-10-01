package com.zergatul.cheatutils.common.events;

import net.minecraft.core.BlockPos;

public class ContinueDestroyBlockEvent {

    public final BlockPos pos;
    public boolean markNewDestroy;

    public ContinueDestroyBlockEvent(BlockPos pos) {
        this.pos = pos;
    }
}