package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private final BlockState[] palette;
    private final Map<BlockState, Integer> reversePalette;

    public SchematicFile(byte[] data) throws IOException, InvalidFormatException {
        this(NbtIo.readCompressed(new ByteArrayInputStream(data)));
    }

    public SchematicFile(int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;

        blocks = new byte[width * height * length];
        summary = new int[256];
        palette = new BlockState[256];

        palette[0] = Blocks.AIR.defaultBlockState();
        reversePalette = createReversePalette();
        summary[0] = blocks.length;

        compound = new CompoundTag();
        compound.putShort(WIDTH_TAG, (short)width);
        compound.putShort(HEIGHT_TAG, (short)height);
        compound.putShort(LENGTH_TAG, (short)length);
        compound.putByteArray(DATA_TAG, new byte[0]);
    }

    private SchematicFile(CompoundTag compound) throws InvalidFormatException {
        validateRequiredTags(compound);
        this.compound = compound;

        width = compound.getShort(WIDTH_TAG);
        height = compound.getShort(HEIGHT_TAG);
        length = compound.getShort(LENGTH_TAG);
        blocks = compound.getByteArray(BLOCKS_TAG);

        validateSize();

        summary = createSummary();
        palette = createPalette();
        reversePalette = createReversePalette();
    }

    private void validateRequiredTags(CompoundTag compound) throws InvalidFormatException {
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

    private void validateSize() throws InvalidFormatException {
        if (blocks.length != width * height * length) {
            throw new InvalidFormatException(
                    String.format("[Blocks] ByteArrayTag length is %s, but it should be %s.",
                            blocks.length,
                            width * height * length));
        }
    }

    private int[] createSummary() {
        int[] summary = new int[256];
        for (byte block : blocks) {
            summary[block & 0xFF]++;
        }
        return summary;
    }

    private BlockState[] createPalette() throws InvalidFormatException {
        BlockState[] palette = new BlockState[256];
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
                        palette[i] = AlphaMapping.blocks[i].defaultBlockState();
                    }
                    return palette;

                default:
                    throw new InvalidFormatException(String.format("Materials type %s is not implemented.", materials));
            }
        }
        palette[0] = Blocks.AIR.defaultBlockState();
        for (int i = 1; i < palette.length; i++) {
            palette[i] = Blocks.OBSIDIAN.defaultBlockState();
        }
        return palette;
    }

    private Map<BlockState, Integer> createReversePalette() {
        Map<BlockState, Integer> map = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            BlockState state = palette[i];
            if (state != null) {
                map.put(state, i);
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
    public BlockState getBlockState(int x, int y, int z) {
        int index = (y * length + z) * width + x;
        return blocks[index] == 0 ? Blocks.AIR.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
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
    public void write(OutputStream output) throws IOException {
        compound.putByteArray(BLOCKS_TAG, blocks);
        NbtIo.writeCompressed(compound, output);
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockState state) throws MissingPaletteEntryException {
        Integer value = reversePalette.get(state);
        if (value == null) {
            throw new MissingPaletteEntryException();
        }

        int index = (y * length + z) * width + x;
        blocks[index] = (byte) (int) value;
    }

    @Override
    public void setPaletteEntry(int index, BlockState state) {
        palette[index] = state;
        reversePalette.put(state, index);
    }
}