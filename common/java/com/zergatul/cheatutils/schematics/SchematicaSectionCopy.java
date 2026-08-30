package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;

public abstract class SchematicaSectionCopy {

    public static final SchematicaSectionCopy EMPTY = new Empty();

    public static SchematicaSectionCopy from(PalettedContainer<BlockState> container) {
        return new Filled(container);
    }

    public abstract BlockState getBlockState(int x, int y, int z);

    private static class Empty extends SchematicaSectionCopy {
        @Override
        public BlockState getBlockState(int x, int y, int z) {
            return Blocks.AIR.defaultBlockState();
        }
    }

    private static class Filled extends SchematicaSectionCopy {

        private final PalettedContainer<BlockState> container;

        public Filled(PalettedContainer<BlockState> container) {
            this.container = container;
        }

        @Override
        public BlockState getBlockState(int x, int y, int z) {
            return container.get(x & 0xF, y & 0xF, z & 0xF);
        }
    }
}