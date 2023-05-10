package com.zergatul.cheatutils.schematics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zergatul.cheatutils.common.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;

public class BlockStateMapping {

    private static List<BlockStateMapping> mappings;

    public final BlockState state;
    public final String blockId;
    public final ImmutableMap<Property<?>, Comparable<?>> tags;

    public BlockStateMapping(BlockState state) {
        this.state = state;
        this.blockId = Registries.BLOCKS.getKey(state.getBlock()).toString();
        this.tags = state.getValues();
    }

    public static synchronized List<BlockStateMapping> get() {
        if (mappings == null) {
            mappings = new ArrayList<>();
            for (Block block : Registries.BLOCKS.getValues()) {
                ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
                for (BlockState blockState : states) {
                    mappings.add(new BlockStateMapping(blockState));
                }
            }
        }

        return mappings;
    }
}