package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.utils.BlockStateMapper;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public class LitematicaOutputFile {

    public static Schematica.DownloadInfo create(SchematicaOutputData data) {
        CompoundTag root = new CompoundTag();
        root.putInt("MinecraftDataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        root.putInt("Version", 6);
        root.putInt("SubVersion", 1);
        root.put("Metadata", createMetadata(data));
        root.put("Regions", createRegions(data));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(root, stream);
        } catch (IOException e) {
            return Schematica.DownloadInfo.of("IOException: " + e.getMessage());
        }

        return Schematica.DownloadInfo.of(stream.toByteArray());
    }

    private static CompoundTag createMetadata(SchematicaOutputData data) {
        int totalBlocks = 0;
        for (int index : data.blocks()) {
            if (index != 0) {
                totalBlocks++;
            }
        }

        long time = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        CompoundTag compound = new CompoundTag();
        compound.putString("Name", "cheatutils");
        compound.putString("Author", "cheatutils");
        compound.putString("Description", "cheatutils");
        compound.putInt("RegionCount", 1);
        compound.putInt("TotalVolume", data.width() * data.height() * data.length());
        compound.putInt("TotalBlocks", totalBlocks);
        compound.putLong("TimeCreated", time);
        compound.putLong("TimeModified", time);
        compound.put("EnclosingSize", vec3i(data.width(), data.height(), data.length()));

        return compound;
    }

    private static CompoundTag createRegions(SchematicaOutputData data) {
        CompoundTag compound = new CompoundTag();
        compound.put("Region1", createRegion(data));
        return compound;
    }

    private static CompoundTag createRegion(SchematicaOutputData data) {
        CompoundTag compound = new CompoundTag();
        compound.put("BlockStatePalette", createPalette(data.palette()));
        compound.put("BlockStates", createBlockStates(data.blocks(), data.palette().size()));
        compound.put("TileEntities", new ListTag());
        compound.put("Position", vec3i(0, 0, 0));
        compound.put("Size", vec3i(data.width(), data.height(), data.length()));
        return compound;
    }

    private static ListTag createPalette(List<BlockState> palette) {
        ListTag list = new ListTag();
        for (BlockState state : palette) {
            list.add(BlockStateMapper.serialize(state));
        }
        return list;
    }

    private static LongArrayTag createBlockStates(int[] blocks, int paletteSize) {
        int bitSize = 32 - Integer.numberOfLeadingZeros(paletteSize);
        int bitMask = (1 << bitSize) - 1;

        long[] data = new long[(blocks.length * bitSize + 63) / 64];
        int bitIndex = 0;
        for (int i = 0; i < blocks.length; i++) {
            int value = blocks[i] & bitMask;
            int startLong = bitIndex >>> 6;
            int endLong = (bitIndex + bitSize - 1) >>> 6;
            int startOffset = bitIndex & 63;
            if (startLong == endLong) {
                data[startLong] |= ((long) value) << startOffset;
            } else {
                int bitsInFirstLong = 64 - startOffset;
                data[startLong] |= ((long) value) << startOffset;
                data[endLong] |= ((long) value) >> bitsInFirstLong;
            }
            bitIndex += bitSize;
        }

        return new LongArrayTag(data);
    }

    private static CompoundTag vec3i(int x, int y, int z) {
        CompoundTag compound = new CompoundTag();
        compound.putInt("x", x);
        compound.putInt("y", y);
        compound.putInt("z", z);
        return compound;
    }
}