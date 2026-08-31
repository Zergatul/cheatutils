package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.BlockStateMapper;
import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.util.Arrays;

public class LitematicaFile implements SchemaFile {

    private static final String VERSION_TAG = "Version";
    private static final String DATA_VERSION_TAG = "MinecraftDataVersion";
    private static final String METADATA_TAG = "Metadata";
    private static final String REGIONS_TAG = "Regions";

    private static final String BLOCK_STATES_TAG = "BlockStates";
    private static final String POSITION_TAG = "Position";
    private static final String BLOCK_STATE_PALETTE_TAG = "BlockStatePalette";
    private static final String SIZE_TAG = "Size";
    private static final String X_TAG = "x";
    private static final String Y_TAG = "y";
    private static final String Z_TAG = "z";

    private final int version;
    private final int dataVersion;
    private final Region[] regions;

    private LitematicaFile(int version, int dataVersion, Region[] regions) {
        this.version = version;
        this.dataVersion = dataVersion;
        this.regions = regions;
    }

    public static LitematicaFile parse(byte[] data) throws InvalidFormatException {
        try {
            return parse(NbtUtils.readCompressed(data, MAX_NBT_SIZE));
        } catch (IOException ex) {
            throw new InvalidFormatException("Cannot parse NBT.", ex);
        }
    }

    @Override
    public int getWidth() {
        return regions[0].getWidth();
    }

    @Override
    public int getHeight() {
        return regions[0].getHeight();
    }

    @Override
    public int getLength() {
        return regions[0].getLength();
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return regions[0].getBlockState(x, y, z);
    }

    @Override
    public int[] getSummary() {
        return regions[0].getSummary();
    }

    @Override
    public BlockState[] getPalette() {
        return regions[0].palette;
    }

    @Override
    public String[] getRawPalette() {
        return regions[0].rawPalette;
    }

    private static LitematicaFile parse(CompoundTag compound) throws InvalidFormatException {
        validateRequiredTags(compound);

        int version = compound.getInt(VERSION_TAG);
        int dataVersion = compound.getInt(DATA_VERSION_TAG);

        CompoundTag regionCompounds = compound.getCompound(REGIONS_TAG);
        Region[] regions = new Region[regionCompounds.size()];
        int index = 0;
        for (String key : regionCompounds.getAllKeys()) {
            regions[index++] = Region.parse(key, regionCompounds.getCompound(key));
        }

        if (regions.length == 0) {
            throw new InvalidFormatException("File has no regions.");
        }

        if (regions.length > 1) {
            throw new InvalidFormatException("File contains more than 1 regions. Not supported.");
        }

        return new LitematicaFile(version, dataVersion, regions);
    }

    private static void validateRequiredTags(CompoundTag compound) throws InvalidFormatException {
        if (!NbtUtils.hasInt(compound, VERSION_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] IntTag is required.", VERSION_TAG));
        }
        if (!NbtUtils.hasInt(compound, DATA_VERSION_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] IntTag is required.", DATA_VERSION_TAG));
        }
        if (!NbtUtils.hasCompound(compound, METADATA_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] CompoundTag is required.", METADATA_TAG));
        }
        if (!NbtUtils.hasCompound(compound, REGIONS_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] CompoundTag is required.", REGIONS_TAG));
        }

        CompoundTag regions = compound.getCompound(REGIONS_TAG);
        for (String key : regions.getAllKeys()) {
            validateRegion(regions.getCompound(key), key);
        }
    }

    private static void validateRegion(CompoundTag compound, String regionName) throws InvalidFormatException {
        if (!NbtUtils.hasLongs(compound, BLOCK_STATES_TAG)) {
            throw new InvalidFormatException(String.format("Region %s. [%s] LongArrayTag is required.", regionName, BLOCK_STATES_TAG));
        }
        if (!NbtUtils.hasCompound(compound, POSITION_TAG)) {
            throw new InvalidFormatException(String.format("Region %s. [%s] CompoundTag is required.", regionName, POSITION_TAG));
        }
        if (!NbtUtils.hasList(compound, BLOCK_STATE_PALETTE_TAG)) {
            throw new InvalidFormatException(String.format("Region %s. [%s] ListTag is required.", regionName, BLOCK_STATE_PALETTE_TAG));
        }
        if (!NbtUtils.hasCompound(compound, SIZE_TAG)) {
            throw new InvalidFormatException(String.format("Region %s. [%s] CompoundTag is required.", regionName, SIZE_TAG));
        }

        validateVector(compound.getCompound(POSITION_TAG), regionName);
        validateVector(compound.getCompound(SIZE_TAG), regionName);
    }

    private static void validateVector(CompoundTag compound, String regionName) throws InvalidFormatException {
        if (!NbtUtils.hasInt(compound, X_TAG)) {
            throw new InvalidFormatException(String.format("Region %s. [%s] IntTag is required in vector structure.", regionName, X_TAG));
        }
        if (!NbtUtils.hasInt(compound, Y_TAG)) {
            throw new InvalidFormatException(String.format("Region %s. [%s] IntTag is required in vector structure.", regionName, Y_TAG));
        }
        if (!NbtUtils.hasInt(compound, Z_TAG)) {
            throw new InvalidFormatException(String.format("Region %s. [%s] IntTag is required in vector structure.", regionName, Z_TAG));
        }
    }

    private static class Region {

        public final String name;
        private final int width;
        private final int height;
        private final int length;
        private final BlockState[] palette;
        private final String[] rawPalette;
        private final long[] blocks;
        private final int bitSize;
        private final long bitMask;
        private final int[] summary;

        private Region(String name, int width, int height, int length, PaletteEntry[] palette, long[] blocks) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.length = length;
            this.palette = Arrays.stream(palette).map(PaletteEntry::state).toArray(BlockState[]::new);
            this.rawPalette = Arrays.stream(palette).map(PaletteEntry::raw).toArray(String[]::new);
            this.blocks = blocks;
            this.bitSize = 32 - Integer.numberOfLeadingZeros(palette.length);
            this.bitMask = (1L << bitSize) - 1L;

            int[] summary = new int[palette.length];
            int size = width * height * length;
            for (int i = 0; i < size; i++) {
                summary[getBlockStateIndex(i)]++;
            }
            this.summary = summary;
        }

        public static Region parse(String name, CompoundTag compound) {
            CompoundTag sizeTag = compound.getCompound(SIZE_TAG);
            int width = Math.abs(sizeTag.getInt(X_TAG));
            int height = Math.abs(sizeTag.getInt(Y_TAG));
            int length = Math.abs(sizeTag.getInt(Z_TAG));

            PaletteEntry[] palette = parsePalette(compound.getList(BLOCK_STATE_PALETTE_TAG, Tag.TAG_COMPOUND));
            long[] blocks = compound.getLongArray(BLOCK_STATES_TAG);

            return new Region(name, width, height, length, palette, blocks);
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getLength() {
            return length;
        }

        public BlockState getBlockState(int x, int y, int z) {
            return palette[getBlockStateIndex(x, y, z)];
        }

        public int[] getSummary() {
            return summary;
        }

        private static PaletteEntry[] parsePalette(ListTag list) {
            PaletteEntry[] palette = new PaletteEntry[list.size()];
            for (int i = 0; i < list.size(); i++) {
                CompoundTag compound = list.getCompound(i);
                palette[i] = new PaletteEntry(compound.toString(), BlockStateMapper.map(compound));
            }
            return palette;
        }

        private int getBlockStateIndex(int x, int y, int z) {
            return getBlockStateIndex(((long) y * length + z) * width + x);
        }

        private int getBlockStateIndex(long index) {
            long startOffset = index * bitSize;
            int startArrIndex = (int) (startOffset >> 6); // startOffset / 64
            int endArrIndex = (int) (((index + 1L) * (long) bitSize - 1L) >> 6);
            int startBitOffset = (int) (startOffset & 0x3F); // startOffset % 64

            if (startArrIndex == endArrIndex)
            {
                return (int) (blocks[startArrIndex] >>> startBitOffset & bitMask);
            }
            else
            {
                int endOffset = 64 - startBitOffset;
                return (int) ((blocks[startArrIndex] >>> startBitOffset | blocks[endArrIndex] << endOffset) & bitMask);
            }
        }
    }
}