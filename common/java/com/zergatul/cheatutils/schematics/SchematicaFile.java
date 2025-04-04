package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class SchematicaFile implements SchemaFile {

    private static final String WIDTH_TAG = "Width";
    private static final String HEIGHT_TAG = "Height";
    private static final String LENGTH_TAG = "Length";
    private static final String BLOCKS_TAG = "Blocks";
    private static final String DATA_TAG = "Data";

    private static final String SCHEMATICA_MAPPING_TAG = "SchematicaMapping";
    private static final String BLOCK_IDS_TAG = "BlockIDs";
    private static final String MATERIALS_TAG = "Materials";

    private final int width;
    private final int height;
    private final int length;
    private final byte[] blocks;
    private final byte[] data;
    private final int[] summary;
    private final BlockState[] palette;
    private final String[] rawPalette;

    private SchematicaFile(int width, int height, int length, byte[] blocks, byte[] data, PaletteEntry[] palette) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = blocks;
        this.data = data;
        this.palette = Arrays.stream(palette).map(PaletteEntry::state).toArray(BlockState[]::new);
        this.rawPalette = Arrays.stream(palette).map(PaletteEntry::raw).toArray(String[]::new);

        int[] summary = new int[4096];
        int size = width * height * length;
        for (int i = 0; i < size; i++) {
            summary[getBlockStateIndex(i)]++;
        }

        this.summary = summary;
    }

    public static SchematicaFile parse(byte[] data) throws InvalidFormatException {
        try {
            return parse(NbtIo.readCompressed(new ByteArrayInputStream(data), NbtAccounter.create(MAX_NBT_SIZE)));
        } catch (IOException ex) {
            throw new InvalidFormatException("Cannot parse NBT.", ex);
        }
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
        return palette[getBlockStateIndex((y * length + z) * width + x)];
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

    private static SchematicaFile parse(CompoundTag compound) throws InvalidFormatException {
        validateRequiredTags(compound);

        int width = compound.getShort(WIDTH_TAG);
        int height = compound.getShort(HEIGHT_TAG);
        int length = compound.getShort(LENGTH_TAG);
        byte[] blocks = compound.getByteArray(BLOCKS_TAG);
        byte[] data = compound.getByteArray(DATA_TAG);

        int size = width * height * length;
        if (blocks.length != size) {
            throw new InvalidFormatException(
                    String.format("[%s] ByteArrayTag length is %s, but it should be %s.",
                            BLOCKS_TAG,
                            blocks.length,
                            size));
        }
        if (data.length != size) {
            throw new InvalidFormatException(
                    String.format("[%s] ByteArrayTag length is %s, but it should be %s.",
                            DATA_TAG,
                            data.length,
                            size));
        }

        PaletteEntry[] palette = createPalette(compound);
        return new SchematicaFile(width, height, length, blocks, data, palette);
    }

    private static PaletteEntry[] createPalette(CompoundTag compound) throws InvalidFormatException {
        if (NbtUtils.hasCompound(compound, SCHEMATICA_MAPPING_TAG)) {
            /*CompoundTag mapping = compound.getCompound(SCHEMATICA_MAPPING);
            BlockState[] palette = new BlockState[4096];
            for (String key : mapping.keySet()) {
                int index = mapping.getShort(key);
                if (index < 0 || index >= palette.length) {
                    throw new InvalidFormatException("Invalid palette index.");
                }
            }*/
            throw new InvalidFormatException(String.format("Failed to create palette. %s tag is not implemented.", SCHEMATICA_MAPPING_TAG));
        }
        if (NbtUtils.hasCompound(compound, BLOCK_IDS_TAG)) {
            throw new InvalidFormatException(String.format("Failed to create palette. %s tag is not implemented.", BLOCK_IDS_TAG));
        }
        if (NbtUtils.hasString(compound, MATERIALS_TAG)) {
            String materials = compound.getString(MATERIALS_TAG);
            if (materials.equals("Alpha")) {
                BlockState[] mapping = VanillaMapping.get();
                PaletteEntry[] palette = new PaletteEntry[mapping.length];
                for (int i = 0; i < palette.length; i++) {
                    palette[i] = new PaletteEntry("#" + i, mapping[i]);
                }
                return palette;
            }
            throw new InvalidFormatException(String.format("Failed to create palette. %s=%s is not implemented.", MATERIALS_TAG, materials));
        }

        throw new InvalidFormatException("Failed to parse palette. Unexpected keys in the root compound tag.");
    }

    private static void validateRequiredTags(CompoundTag compound) throws InvalidFormatException {
        if (!NbtUtils.hasShort(compound, WIDTH_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [Width] ShortTag is required.");
        }
        if (!NbtUtils.hasShort(compound, HEIGHT_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [Height] ShortTag is required.");
        }
        if (!NbtUtils.hasShort(compound, LENGTH_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [Length] ShortTag is required.");
        }
        if (!NbtUtils.hasBytes(compound, BLOCKS_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [Blocks] ByteArrayTag is required.");
        }
        if (!NbtUtils.hasBytes(compound, DATA_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [Data] ByteArrayTag is required.");
        }
    }

    private int getBlockStateIndex(int index) {
        return (Byte.toUnsignedInt(blocks[index]) << 4) | (Byte.toUnsignedInt(data[index]) & 0x0F);
    }
}