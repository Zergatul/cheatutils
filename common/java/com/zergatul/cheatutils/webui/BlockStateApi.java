package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.common.Registries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockStateApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-state";
    }

    @Override
    public String get() {
        List<BlockState> states = Registries.BLOCKS.getValues()
                .stream()
                .flatMap(b -> b.getStateDefinition().getPossibleStates().stream())
                .toList();
        return gson.toJson(states);
    }
}