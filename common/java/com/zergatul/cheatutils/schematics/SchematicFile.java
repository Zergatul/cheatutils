package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
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
    private final byte[] data;
    private final int[] summary;
    private final BlockState[] palette;
    private final Map<BlockState, Integer> reversePalette;

    public SchematicFile(byte[] data) throws IOException, InvalidFormatException {
        this(NbtIo.readCompressed(new ByteArrayInputStream(data), NbtAccounter.create(104857600L)));
    }

    public SchematicFile(int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;

        blocks = new byte[width * height * length];
        data = new byte[width * height * length];
        summary = new int[256];
        palette = new BlockState[256];

        palette[0] = Blocks.AIR.defaultBlockState();
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
        data = compound.getByteArray(DATA_TAG);

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
        /*if (!NbtUtils.hasBytes(compound, DATA_TAG)) {
            throw new InvalidFormatException("Invalid NBT structure. [Data] ByteArrayTag is required.");
        }*/
    }

    private void ValidateSize() throws InvalidFormatException {
        int size = width * height * length;
        if (blocks.length != size) {
            throw new InvalidFormatException(
                    String.format("[Blocks] ByteArrayTag length is %s, but it should be %s.",
                            blocks.length,
                            size));
        }
    }

    private int[] CreateSummary() {
        int[] summary = new int[65536];
        int size = width * height * length;
        for (int i = 0; i < size; i++) {
            summary[(Byte.toUnsignedInt(blocks[i]) << 8) | getData(i)]++;
        }
        return summary;
    }

    private BlockState[] CreatePalette() throws InvalidFormatException {
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
                    return AlphaMapping.get();

                default:
                    throw new InvalidFormatException(String.format("Materials type %s is not implemented.", materials));
            }
        }

        return AlphaMapping.get();
    }

    private Map<BlockState, Integer> CreateReversePalette() {
        Map<BlockState, Integer> map = new HashMap<>();
        for (int i = 0; i < palette.length; i++) {
            BlockState state = palette[i];
            if (state != null && !map.containsKey(state)) {
                map.put(state, i);
            }
        }
        return map;
    }

    private int getData(int index) {
        return index < data.length ? Byte.toUnsignedInt(data[index]) : 0;
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
        return palette[(Byte.toUnsignedInt(blocks[index]) << 8) | getData(index)];
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