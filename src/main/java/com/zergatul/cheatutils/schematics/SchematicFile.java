package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SchematicFile implements SchemaFileEditable {

    private static final String WIDTH_TAG = "Width";
    private static final String HEIGHT_TAG = "Height";
    private static final String LENGTH_TAG = "Length";
    private static final String BLOCKS_TAG = "Blocks";
    private static final String DATA_TAG = "Data";

    private final CompoundTag compound;
    private final int width;
    private final int height;
    private final int length;
    private final byte[] blocks;
    private final int[] summary;
    private final Block[] palette;
    private final Map<Block, Integer> reversePalette;

    public SchematicFile(byte[] data) throws IOException, InvalidFormatException {
        this(NbtIo.readCompressed(new ByteArrayInputStream(data)));
    }

    public SchematicFile(int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;

        blocks = new byte[width * height * length];
        summary = new int[256];
        palette = new Block[256];

        palette[0] = Blocks.AIR;
        reversePalette = CreateReversePalette();
        summary[0] = blocks.length;

        compound = new CompoundTag();
        compound.putShort(WIDTH_TAG, (short)width);
        compound.putShort(HEIGHT_TAG, (short)height);
        compound.putShort(LENGTH_TAG, (short)length);
        compound.putByteArray(DATA_TAG, new byte[0]);
    }

    private SchematicFile(CompoundTag compound) throws InvalidFormatException {
        ValidateRequiredTags(compound);
        this.compound = compound;

        width = compound.getShort(WIDTH_TAG);
        height = compound.getShort(HEIGHT_TAG);
        length = compound.getShort(LENGTH_TAG);
        blocks = compound.getByteArray(BLOCKS_TAG);

        ValidateSize();

        summary = CreateSummary();
        palette = CreatePalette();
        reversePalette = CreateReversePalette();
    }

    private void ValidateRequiredTags(CompoundTag compound) throws InvalidFormatException {
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

    private void ValidateSize() throws InvalidFormatException {
        if (blocks.length != width * height * length) {
            throw new InvalidFormatException(
                    String.format("[Blocks] ByteArrayTag length is %s, but it should be %s.",
                            blocks.length,
                            width * height * length));
        }
    }

    private int[] CreateSummary() {
        int[] summary = new int[256];
        for (byte block: blocks) {
            summary[block & 0xFF]++;
        }
        return summary;
    }

    private Block[] CreatePalette() throws InvalidFormatException {
        Block[] palette = new Block[256];
        if (NbtUtils.hasCompound(compound, "SchematicaMapping")) {
            throw new InvalidFormatException("Not implemented");
        }
        if (NbtUtils.hasCompound(compound, "BlockIDs")) {
            throw new InvalidFormatException("Not implemented");
        }
        if (NbtUtils.hasString(compound, "Materials")) {
            String materials = compound.getString("Materials");
            switch (materials) {
                case "Alpha":
                    for (int i = 0; i < AlphaMapping.blocks.length; i++) {
                        palette[i] = AlphaMapping.blocks[i];
                    }
                    return palette;

                default:
                    throw new InvalidFormatException(String.format("Materials type %s is not implemented.", materials));
            }
        }
        palette[0] = Blocks.AIR;
        for (int i = 1; i < palette.length; i++) {
            palette[i] = Blocks.OBSIDIAN;
        }
        return palette;
    }

    private Map<Block, Integer> CreateReversePalette() {
        Map<Block, Integer> map = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            Block block = palette[i];
            if (block != null) {
                map.put(block, i);
            }
        }
        return map;
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
    public Block getBlock(int x, int y, int z) {
        int index = (y * length + z) * width + x;
        return blocks[index] == 0 ? Blocks.AIR : Blocks.OBSIDIAN;
    }

    @Override
    public int[] getSummary() {
        return summary;
    }

    @Override
    public Block[] getPalette() {
        return palette;
    }

    @Override
    public void write(OutputStream output) throws IOException {
        compound.putByteArray(BLOCKS_TAG, blocks);
        NbtIo.writeCompressed(compound, output);
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        Integer value = reversePalette.get(block);
        if (value == null) {
            return;
        }

        int index = (y * length + z) * width + x;
        blocks[index] = (byte) (int) value;
    }

    @Override
    public void setPaletteEntry(int index, Block block) {
        palette[index] = block;
        reversePalette.put(block, index);
    }
}