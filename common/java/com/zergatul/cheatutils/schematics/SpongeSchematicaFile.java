package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SpongeSchematicaFile {

    private static final String VERSION_TAG = "Version";

    public static SchemaFile parse(byte[] data) throws InvalidFormatException {
        try {
            return parse(NbtIo.readCompressed(new ByteArrayInputStream(data), NbtAccounter.create(SchemaFile.MAX_NBT_SIZE)));
        } catch (IOException ex) {
            throw new InvalidFormatException("Cannot parse NBT.", ex);
        }
    }

    private static SchemaFile parse(CompoundTag compound) throws InvalidFormatException {
        if (!NbtUtils.hasInt(compound, VERSION_TAG)) {
            throw new InvalidFormatException(String.format("Invalid NBT structure. [%s] IntTag is required.", VERSION_TAG));
        }

        int version = compound.getInt(VERSION_TAG);
        if (version < 3) {
            return SpongeSchematicaVersion1File.parse(compound);
        }

        throw new InvalidFormatException(String.format("Sponge Schematica File version=%d is not supported.", version));
    }
}