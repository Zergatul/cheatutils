package com.zergatul.cheatutils.schematics;

import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.utils.BlockStateMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

// https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-1.md
public class SpongeSchematicaVersion1OutputFile {

    public static Schematica.DownloadInfo create(SchematicaOutputData data) {
        CompoundTag root = new CompoundTag();
        root.putInt("Version", 1);
        root.putShort("Width", (short) data.width());
        root.putShort("Height", (short) data.height());
        root.putShort("Length", (short) data.length());
        root.put("Palette", createPalette(data.palette()));
        root.putByteArray("BlockData", createBlockData(data.blocks()));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(root, stream);
        } catch (IOException e) {
            return Schematica.DownloadInfo.of("IOException: " + e.getMessage());
        }

        return Schematica.DownloadInfo.of(stream.toByteArray());
    }

    private static CompoundTag createPalette(List<BlockState> palette) {
        CompoundTag compound = new CompoundTag();
        for (int i = 0; i < palette.size(); i++) {
            compound.putInt(BlockStateMapper.serializeAsString(palette.get(i)), i);
        }
        return compound;
    }

    private static byte[] createBlockData(int[] blocks) {
        return VarIntFormat.encode(blocks);
    }
}