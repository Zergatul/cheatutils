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
import java.nio.file.Path;

public class SchematicFile implements SchemaFile {

    private final CompoundTag compound;
    private final int width;
    private final int height;
    private final int length;
    private final byte[] blocks;
    private final int[] summary;
    private final Block[] palette;

    public SchematicFile(byte[] data) throws IOException, InvalidFormatException {
        this(NbtIo.readCompressed(new ByteArrayInputStream(data)));
    }

    private SchematicFile(CompoundTag compound) throws InvalidFormatException {
        ValidateRequiredTags(compound);
        this.compound = compound;

        width = compound.getShort("Width");
        height = compound.getShort("Height");
        length = compound.getShort("Length");
        blocks = compound.getByteArray("Blocks");

        ValidateSize();

        summary = CreateSummary();
        palette = CreatePalette();
    }

    private void ValidateRequiredTags(CompoundTag compound) throws InvalidFormatException {
        if (!NbtUtils.hasShort(compound, "Width")) {
            throw new InvalidFormatException("Invalid NBT structure. [Width] ShortTag is required.");
        }
        if (!NbtUtils.hasShort(compound, "Height")) {
            throw new InvalidFormatException("Invalid NBT structure. [Height] ShortTag is required.");
        }
        if (!NbtUtils.hasShort(compound, "Length")) {
            throw new InvalidFormatException("Invalid NBT structure. [Length] ShortTag is required.");
        }
        if (!NbtUtils.hasBytes(compound, "Blocks")) {
            throw new InvalidFormatException("Invalid NBT structure. [Blocks] ByteArrayTag is required.");
        }
        if (!NbtUtils.hasBytes(compound, "Data")) {
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
}