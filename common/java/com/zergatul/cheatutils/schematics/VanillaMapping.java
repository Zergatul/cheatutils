package com.zergatul.cheatutils.schematics;

import com.mojang.serialization.Dynamic;
import com.zergatul.cheatutils.utils.BlockStateMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class VanillaMapping {

    private static BlockState[] palette;

    public static synchronized BlockState[] get() {
        fill();
        return palette;
    }

    private static void fill() {
        if (palette != null) {
            return;
        }

        palette = new BlockState[4096];
        for (int i = 0; i < palette.length; i++) {
            Dynamic<?> dynamic = BlockStateData.getTag(i);
            if (dynamic.getOps() == NbtOps.INSTANCE) {
                CompoundTag compound = (CompoundTag) dynamic.getValue();
                palette[i] = BlockStateMapper.map(compound);
            } else {
                palette[i] = Blocks.AIR.defaultBlockState();
            }
        }
    }
}