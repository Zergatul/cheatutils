package com.zergatul.cheatutils.schematics;

import com.google.common.collect.ImmutableMap;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.core.IdMapper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BlockStateMapping {

    private static List<BlockStateMapping> mappings;

    public final BlockState state;
    public final String blockId;
    public final ImmutableMap<Property<?>, Comparable<?>> tags;

    public BlockStateMapping(BlockState state) {
        this.state = state;
        this.blockId = ModApiWrapper.BLOCKS.getKey(state.getBlock()).toString();
        this.tags = state.getValues();
    }

    public static synchronized List<BlockStateMapping> get() {
        if (mappings == null) {
            IdMapper<BlockState> mapper = net.minecraftforge.registries.GameData.getBlockStateIDMap();
            Stream<BlockState> stream = StreamSupport.stream(mapper.spliterator(), false);
            mappings = stream.map(BlockStateMapping::new).toList();
        }

        return mappings;
    }
}