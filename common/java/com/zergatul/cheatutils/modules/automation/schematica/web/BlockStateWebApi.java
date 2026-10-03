package com.zergatul.cheatutils.modules.automation.schematica.web;

import com.zergatul.cheatutils.common.RegistryExtensions;
import com.zergatul.cheatutils.web.WebApiBase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockStateWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "block-state";
    }

    @Override
    public String get() {
        List<BlockState> states = RegistryExtensions.getValues(BuiltInRegistries.BLOCK)
                .stream()
                .flatMap(b -> b.getStateDefinition().getPossibleStates().stream())
                .toList();
        return gson.toJson(states);
    }
}