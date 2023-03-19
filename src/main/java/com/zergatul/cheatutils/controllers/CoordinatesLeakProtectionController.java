package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;

public class CoordinatesLeakProtectionController {

    public static final CoordinatesLeakProtectionController instance = new CoordinatesLeakProtectionController();

    private final Random random = new Random();

    private CoordinatesLeakProtectionController() {

    }

    public void processChunk(LevelChunk chunk) {
        if (ConfigStore.instance.getConfig().coordinateLeakProtectionConfig.enabled) {
            messUpBedrock(chunk);
        }
    }

    private void messUpBedrock(LevelChunk chunk) {
        BlockState[][] cols = new BlockState[256][];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            pos.setX(x);
            for (int z = 0; z < 16; z++) {
                pos.setZ(z);
                BlockState[] col = new BlockState[5];
                cols[(x << 4) | z] = col;
                for (int y = -64; y <= -60; y++) {
                    pos.setY(y);
                    col[y + 64] = chunk.getBlockState(pos);
                }
            }
        }
        ArrayUtils.shuffle(cols, random);

        for (int x = 0; x < 16; x++) {
            pos.setX(x);
            for (int z = 0; z < 16; z++) {
                pos.setZ(z);
                BlockState[] col = cols[(x << 4) | z];
                for (int y = -64; y <= -60; y++) {
                    pos.setY(y);
                    chunk.setBlockState(pos, col[y + 64], false);
                }
            }
        }
    }
}