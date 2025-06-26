package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.modules.BlockEspEvent;
import com.zergatul.cheatutils.scripting.types.BlockPosWrapper;

@FunctionalInterface
public interface BlockEspConsumer {
    void accept(BlockPosWrapper pos, BlockEspEvent event);
}