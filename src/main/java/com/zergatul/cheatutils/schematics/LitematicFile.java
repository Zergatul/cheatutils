package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.collections.BitArray;
import com.zergatul.cheatutils.utils.NbtUtils;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LitematicFile implements SchemaFile {

    private final CompoundTag compound;
    private final int version;
    private final int subVersion;
    private final int dataVersion;
    private final Region[] regions;
    private final int width;
    private final int height;
    private final int length;

    public LitematicFile(byte[] data) throws IOException, InvalidFormatException {
        this(NbtIo.readCompressed(new ByteArrayInputStream(data)));
    }

    private LitematicFile(CompoundTag compound) throws InvalidFormatException {
        ValidateRequiredTags(compound);
        this.compound = compound;

        version = compound.getInt("Version");
        subVersion = compound.getInt("SubVersion");
        dataVersion = compound.getInt("MinecraftDataVersion");

        CompoundTag regionCompounds = compound.getCompound("Regions");
        regions = new Region[regionCompounds.size()];
        int index = 0;
        for (String key : regionCompounds.getAllKeys()) {
            regions[index++] = new Region(key, regionCompounds.getCompound(key));
        }

        if (regions.length == 0) {
            throw new InvalidFormatException("Zero regions.");
        }

        if (regions.length > 1) {
            throw new InvalidFormatException("More than 1 regions. Not supported.");
        }

        width = regions[0].width;
        height = regions[0].height;
        length = regions[0].length;
    }

    private void ValidateRequiredTags(CompoundTag compound) throws InvalidFormatException {
        if (!NbtUtils.hasInt(compound, "Version")) {
            throw new InvalidFormatException("Invalid NBT structure. [Version] IntTag is required.");
        }
        /*if (!NbtUtils.hasInt(compound, "SubVersion")) {
            throw new InvalidFormatException("Invalid NBT structure. [SubVersion] IntTag is required.");
        }*/
        if (!NbtUtils.hasInt(compound, "MinecraftDataVersion")) {
            throw new InvalidFormatException("Invalid NBT structure. [MinecraftDataVersion] IntTag is required.");
        }
        if (!NbtUtils.hasCompound(compound, "Metadata")) {
            throw new InvalidFormatException("Invalid NBT structure. [Metadata] CompoundTag is required.");
        }
        if (!NbtUtils.hasCompound(compound, "Regions")) {
            throw new InvalidFormatException("Invalid NBT structure. [Regions] CompoundTag is required.");
        }

        CompoundTag regions = compound.getCompound("Regions");
        for (String key : regions.getAllKeys()) {
            ValidateRegion(regions.getCompound(key), String.format("Invalid NBT structure in %s region", key));
        }
    }

    private void ValidateRegion(CompoundTag compound, String errorPrefix) throws InvalidFormatException {
        if (!NbtUtils.hasLongs(compound, "BlockStates")) {
            throw new InvalidFormatException(String.format("%s. [BlockStates] LongArrayTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasCompound(compound, "Position")) {
            throw new InvalidFormatException(String.format("%s. [Position] CompoundTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasList(compound, "BlockStatePalette")) {
            throw new InvalidFormatException(String.format("%s. [BlockStatePalette] ListTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasCompound(compound, "Size")) {
            throw new InvalidFormatException(String.format("%s. [Size] CompoundTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasList(compound, "TileEntities")) {
            throw new InvalidFormatException(String.format("%s. [TileEntities] ListTag is required.", errorPrefix));
        }

        ValidateVector(compound.getCompound("Position"), errorPrefix + ", [Position] tag");
        ValidateVector(compound.getCompound("Size"), errorPrefix + ", [Size] tag");
    }

    private void ValidateVector(CompoundTag compound, String errorPrefix) throws InvalidFormatException {
        if (!NbtUtils.hasInt(compound, "x")) {
            throw new InvalidFormatException(String.format("%s. [x] IntTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasInt(compound, "y")) {
            throw new InvalidFormatException(String.format("%s. [x] IntTag is required.", errorPrefix));
        }
        if (!NbtUtils.hasInt(compound, "z")) {
            throw new InvalidFormatException(String.format("%s. [x] IntTag is required.", errorPrefix));
        }
    }

    @Override
    public int getWidth() {
        return regions[0].width;
    }

    @Override
    public int getHeight() {
        return regions[0].height;
    }

    @Override
    public int getLength() {
        return regions[0].length;
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return regions[0].getBlockState(x, y, z);
    }

    @Override
    public int[] getSummary() {
        return regions[0].summary;
    }

    @Override
    public BlockState[] getPalette() {
        return regions[0].palette;
    }

    @Override
    public void write(OutputStream output) {

    }

    private static class Region {

        public final String name;
        private final CompoundTag compound;
        private final int width;
        private final int height;
        private final int length;
        private final BlockState[] palette;
        private final BitArray blocks;
        private final int[] summary;

        public Region(String name, CompoundTag compound) throws InvalidFormatException {
            this.name = name;
            this.compound = compound;

            CompoundTag sizeTag = compound.getCompound("Size");
            width = Math.abs(sizeTag.getInt("x"));
            height = Math.abs(sizeTag.getInt("y"));
            length = Math.abs(sizeTag.getInt("z"));

            palette = parsePalette((ListTag) compound.get("BlockStatePalette"));

            int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(palette.length - 1));
            blocks = new BitArray(compound.getLongArray("BlockStates"), bits);

            if (blocks.size() < width * height * length) {
                throw new InvalidFormatException("[BlockStates] size mismatch.");
            }

            summary = createSummary();
        }

        public BlockState getBlockState(int x, int y, int z) {
            int index = (y * length + z) * width + x;
            return palette[blocks.get(index)];
        }

        private BlockState[] parsePalette(ListTag list) throws InvalidFormatException {
            BlockState[] palette = new BlockState[list.size()];
            for (int i = 0; i < list.size(); i++) {
                CompoundTag item = (CompoundTag) list.get(i);
                String blockId = item.getString("Name");
                CompoundTag propertiesTag = item.getCompound("Properties");
                BlockStateMapping mapping = null;
                BlockStateMappingLoop:
                for (BlockStateMapping m : BlockStateMapping.get()) {
                    if (!m.blockId.equals(blockId)) {
                        continue;
                    }

                    if (propertiesTag.size() != m.tags.size()) {
                        continue;
                    }

                    for (String key : propertiesTag.getAllKeys()) {
                        Tag valueTag = propertiesTag.get(key);
                        String value;
                        if (valueTag instanceof StringTag stringTag) {
                            value = stringTag.getAsString();
                        } else {
                            throw new InvalidFormatException("Not implemented.");
                        }

                        Property<?> property = m.tags.keySet().stream().filter(p -> p.getName().equals(key))
                                .findFirst().orElse(null);
                        if (property == null) {
                            continue BlockStateMappingLoop;
                        }
                        if (!value.equalsIgnoreCase(m.tags.get(property).toString())) {
                            continue BlockStateMappingLoop;
                        }
                    }

                    mapping = m;
                    break;
                }

                if (mapping == null) {
                    throw new InvalidFormatException(String.format("Cannot find BlockState [%s]%s.", blockId, propertiesTag));
                }

                palette[i] = mapping.state;
            }
            return palette;
        }

        private int[] createSummary() {
            int[] summary = new int[palette.length];
            int size = width * height * length;
            for (int i = 0; i < size; i++) {
                summary[blocks.get(i)]++;
            }
            return summary;
        }
    }
}