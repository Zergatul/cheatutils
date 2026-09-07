package com.zergatul.cheatutils.modules.esp.blocks;

import com.zergatul.cheatutils.mixins.accessors.BitArrayAccessor;
import com.zergatul.cheatutils.mixins.accessors.BlockStateContainerAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

public class SectionBlockStates {

    public static final SectionBlockStates EMPTY = createEmpty();

    private final IBlockStatePalette palette;
    private final int bitsPerEntry;
    private final long[] storage;

    private SectionBlockStates(IBlockStatePalette palette, int bitsPerEntry, long[] storage) {
        this.palette = palette;
        this.bitsPerEntry = bitsPerEntry;
        this.storage = storage;
    }

    public static SectionBlockStates from(BlockStateContainer section) {
        BlockStateContainerAccessor sectionAccessor = (BlockStateContainerAccessor) section;
        BitArray array = sectionAccessor.getStorage_CU();
        BitArrayAccessor arrayAccessor = (BitArrayAccessor) array;
        int bitsPerEntry = arrayAccessor.getBitsPerEntry_CU();
        long[] storage = clone(array);
        // palette seems immutable
        return new SectionBlockStates(sectionAccessor.getPalette_CU(), bitsPerEntry, storage);
    }

    public IBlockState get(int x, int y, int z) {
        return this.get((y << 8) | (z << 4) | x);
    }

    private static SectionBlockStates createEmpty() {
        return new SectionBlockStates(new EmptyPalette(), 1, new long[16 * 16 * 16 / 8]);
    }

    private IBlockState get(int index) {
        IBlockState state = this.palette.getBlockState(getStorageAt(index));
        return state == null ? Blocks.AIR.getDefaultState() : state;
    }

    private int getStorageAt(int index) {
        int bitsPerEntry = this.bitsPerEntry;
        int i = index * bitsPerEntry;
        int j = i / 64;
        int k = ((index + 1) * bitsPerEntry - 1) / 64;
        int l = i % 64;
        long maxEntryValue = (1L << bitsPerEntry) - 1L;

        if (j == k) {
            return (int)(this.storage[j] >>> l & maxEntryValue);
        } else {
            int i1 = 64 - l;
            return (int)((this.storage[j] >>> l | this.storage[k] << i1) & maxEntryValue);
        }
    }

    private static long[] clone(BitArray array) {
        return array.getBackingLongArray().clone();
    }

    @NullMarked
    private static class EmptyPalette implements IBlockStatePalette {

        @Override
        public int idFor(IBlockState state) {
            return 0;
        }

        @Override
        public @Nullable IBlockState getBlockState(int indexKey) {
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public void read(PacketBuffer buf) {}

        @Override
        public void write(PacketBuffer buf) {}

        @Override
        public int getSerializedSize() {
            return 0;
        }
    }
}