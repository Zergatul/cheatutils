package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.BlockStateMapper;
import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-1.md
public class SpongeSchematicaVersion1File implements SchemaFile {

    private static final String WIDTH_TAG = "Width";
    private static final String HEIGHT_TAG = "Height";
    private static final String LENGTH_TAG = "Length";
    private static final String PALETTE_TAG = "Palette";
    private static final String BLOCK_DATA_TAG = "BlockData";

    private final int width;
    private final int height;
    private final int length;
    private final BlockState[] palette;
    private final String[] rawPalette;
    private final int[] blocks;
    private final int[] summary;

    private SpongeSchematicaVersion1File(int width, int height, int length, PaletteEntry[] palette, int[] blocks) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.palette = Arrays.stream(palette).map(PaletteEntry::state).toArray(BlockState[]::new);
        this.rawPalette = Arrays.stream(palette).map(PaletteEntry::raw).toArray(String[]::new);
        this.blocks = blocks;

        int[] summary = new int[palette.length];
        int size = width * height * length;
        for (int i = 0; i < size; i++) {
            summary[getBlockStateIndex(i)]++;
        }
        this.summary = summary;
    }

    public static SpongeSchematicaVersion1File parse(CompoundTag compound) throws InvalidFormatException {
        validateRequiredTags(compound);

        if (!NbtUtils.hasCompound(compound, PALETTE_TAG)) {
            throw new InvalidFormatException("Files without palette are not supported.");
        }

        int width = compound.getInt(WIDTH_TAG).orElseThrow();
        int height = compound.getInt(HEIGHT_TAG).orElseThrow();
        int length = compound.getInt(LENGTH_TAG).orElseThrow();

        PaletteEntry[] palette = parsePalette(compound.getCompound(PALETTE_TAG).orElseThrow());
        byte[] encodedBlocks = compound.getByteArray(BLOCK_DATA_TAG).orElseThrow();
        int[] blocks = VarIntFormat.decode(encodedBlocks);

        if (blocks.length != width * height * length) {
            throw new InvalidFormatException(String.format("Blocks array size mismatch. Expected: %d, actual: %d.", width * height * length, blocks.length));
        }

        return new SpongeSchematicaVersion1File(width, height, length, palette, blocks);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return palette[getBlockStateIndex(x, y, z)];
    }

    @Override
    public int[] getSummary() {
        return summary;
    }

    @Override
    public BlockState[] getPalette() {
        return palette;
    }

    @Override
    public String[] getRawPalette() {
        return rawPalette;
    }

    private static void validateRequiredTags(CompoundTag compound) throws InvalidFormatException {
        if (!NbtUtils.hasShort(compound, WIDTH_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] ShortTag is required.", WIDTH_TAG));
        }
        if (!NbtUtils.hasShort(compound, HEIGHT_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] ShortTag is required.", HEIGHT_TAG));
        }
        if (!NbtUtils.hasShort(compound, LENGTH_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] ShortTag is required.", LENGTH_TAG));
        }
        if (!NbtUtils.hasBytes(compound, BLOCK_DATA_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] ByteArrayTag is required.", BLOCK_DATA_TAG));
        }
    }

    private static PaletteEntry[] parsePalette(CompoundTag compound) throws InvalidFormatException {
        Map<Integer, PaletteEntry> map = new HashMap<>();
        for (String key : compound.keySet()) {
            BlockState state = BlockStateMapper.map(key);
            int index = compound.getInt(key).orElseThrow();
            map.put(index, new PaletteEntry(key, state));
        }

        int maxIndex = map.keySet().stream().max(Integer::compare).orElseThrow();
        if (maxIndex >= 65536) {
            throw new InvalidFormatException(String.format("Palette index %d too big.", maxIndex));
        }

        PaletteEntry[] palette = new PaletteEntry[maxIndex + 1];
        for (var entry : map.entrySet()) {
            palette[entry.getKey()] = entry.getValue();
        }
        return palette;
    }

    private int getBlockStateIndex(int x, int y, int z) {
        return getBlockStateIndex((y * length + z) * width + x);
    }

    private int getBlockStateIndex(int index) {
        return blocks[index];
    }
}