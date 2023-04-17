package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.controllers.CurrentBlockController;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CurrentBlockApi {

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public int getX() {
        BlockPos pos = CurrentBlockController.instance.getPos();
        return pos == null ? 0 : pos.getX();
    }

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public int getY() {
        BlockPos pos = CurrentBlockController.instance.getPos();
        return pos == null ? 0 : pos.getY();
    }

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public int getZ() {
        BlockPos pos = CurrentBlockController.instance.getPos();
        return pos == null ? 0 : pos.getZ();
    }

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public String getId() {
        BlockState state = CurrentBlockController.instance.getState();
        return state == null ? "" : Registries.BLOCKS.getKey(state.getBlock()).toString();
    }

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public boolean isSource() {
        BlockState state = CurrentBlockController.instance.getState();
        return state != null && state.getFluidState().isSource();
    }
}