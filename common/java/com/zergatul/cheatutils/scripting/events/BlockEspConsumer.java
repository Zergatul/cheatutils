package com.zergatul.cheatutils.scripting.events;

import com.zergatul.cheatutils.scripting.modules.BlockEspEvent;
import com.zergatul.cheatutils.scripting.types.BlockPosWrapper;

@FunctionalInterface
public interface BlockEspConsumer {
    void accept(BlockPosWrapper pos, BlockEspEvent event);
}