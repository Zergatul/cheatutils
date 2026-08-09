package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CurrentBlockApi {

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public int getX() {
        BlockPos pos = BlockAutomation.instance.getCurrentBlockPos();
        return pos == null ? 0 : pos.getX();
    }

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public int getY() {
        BlockPos pos = BlockAutomation.instance.getCurrentBlockPos();
        return pos == null ? 0 : pos.getY();
    }

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public int getZ() {
        BlockPos pos = BlockAutomation.instance.getCurrentBlockPos();
        return pos == null ? 0 : pos.getZ();
    }

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public String getId() {
        BlockState state = BlockAutomation.instance.getCurrentBlockState();
        return state == null ? "" : Registries.BLOCKS.getKey(state.getBlock()).toString();
    }

    @ApiVisibility(ApiType.CURRENT_BLOCK)
    public boolean isSource() {
        BlockState state = BlockAutomation.instance.getCurrentBlockState();
        return state != null && state.getFluidState().isSource();
    }
}