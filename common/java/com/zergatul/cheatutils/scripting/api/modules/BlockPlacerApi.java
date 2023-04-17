package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.controllers.ScriptedBlockPlacerController;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

public class BlockPlacerApi {

    @ApiVisibility(ApiType.BLOCK_PLACER)
    public void setBlockId(String blockId) {
        ScriptedBlockPlacerController.instance.setBlock(blockId);
    }
}