package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.controllers.CurrentBlockController;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import net.minecraft.core.BlockPos;

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
}