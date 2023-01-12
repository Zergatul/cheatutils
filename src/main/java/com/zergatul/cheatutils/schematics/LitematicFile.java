package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Block;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class LitematicFile implements SchemaFile {

    private final CompoundTag compound;

    public LitematicFile(byte[] data) throws IOException, InvalidFormatException {
        this(NbtIo.readCompressed(new ByteArrayInputStream(data)));
    }

    private LitematicFile(CompoundTag compound) throws InvalidFormatException {
        ValidateRequiredTags(compound);
        this.compound = compound;

        /*width = compound.getShort("Width");
        height = compound.getShort("Height");
        length = compound.getShort("Length");
        blocks = compound.getByteArray("Blocks");

        ValidateSize();

        summary = CreateSummary();
        palette = CreatePalette();*/
    }

    private void ValidateRequiredTags(CompoundTag compound) throws InvalidFormatException {
        if (!NbtUtils.hasInt(compound, "Version")) {
            throw new InvalidFormatException("Invalid NBT structure. [Version] IntTag is required.");
        }
        if (!NbtUtils.hasInt(compound, "SubVersion")) {
            throw new InvalidFormatException("Invalid NBT structure. [SubVersion] IntTag is required.");
        }
        if (!NbtUtils.hasInt(compound, "MinecraftDataVersion")) {
            throw new InvalidFormatException("Invalid NBT structure. [MinecraftDataVersion] IntTag is required.");
        }
        if (!NbtUtils.hasCompound(compound, "Metadata")) {
            throw new InvalidFormatException("Invalid NBT structure. [Metadata] CompoundTag is required.");
        }
        if (!NbtUtils.hasCompound(compound, "Regions")) {
            throw new InvalidFormatException("Invalid NBT structure. [Regions] CompoundTag is required.");
        }
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return null;
    }

    @Override
    public int[] getSummary() {
        return new int[0];
    }

    @Override
    public Block[] getPalette() {
        return new Block[0];
    }
}