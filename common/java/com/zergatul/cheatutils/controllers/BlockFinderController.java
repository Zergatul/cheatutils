package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockFinderController {

    public static final BlockFinderController instance = new BlockFinderController();

    // all modification to blocks are done in BlockEvents thread
    public final Map<BlockEspConfig, Set<BlockPos>> blocks = new ConcurrentHashMap<>();

    private BlockFinderController() {
        Events.ChunkLoaded.add(this::onChunkLoaded);
        Events.ChunkUnloaded.add(this::onChunkUnloaded);
        Events.BlockUpdated.add(this::onBlockUpdated);
    }

    public void clear() {
        // TODO: check if necessary
        /*addToQueue(() -> {
            for (BlockEspConfig config: blocks.keySet()) {
                blocks.put(config, ConcurrentHashMap.newKeySet());
            }
        });*/
    }

    public void addConfig(BlockEspConfig config) {
        run(() -> {
            blocks.put(config, ConcurrentHashMap.newKeySet());
            scan(config);
        });
    }

    public void removeConfig(BlockEspConfig config) {
        run(() -> blocks.remove(config));
    }

    public void removeAllConfigs() {
        run(blocks::clear);
    }

    private void run(Runnable runnable) {
        BlockEventsProcessor.instance.getExecutor().execute(runnable);
    }

    private void onChunkLoaded(SnapshotChunk chunk) {
        // need to call unload?
        Map<Block, BlockEspConfig> map = ConfigStore.instance.getConfig().blocks.getMap();
        int minY = chunk.getMinY();
        int xc = chunk.getPos().x << 4;
        int zc = chunk.getPos().z << 4;
        for (int x = 0; x < 16; x++) {
            int xw = xc | x;
            for (int z = 0; z < 16; z++) {
                int zw = zc | z;
                int height = chunk.getHeight(x, z);
                for (int y = minY; y <= height; y++) {
                    BlockState state = chunk.getBlockState(x, y, z);
                    checkBlock(xw, y, zw, state, map);
                }
            }
        }
    }

    private void onChunkUnloaded(ChunkPos pos) {
        final int cx = pos.x;
        final int cz = pos.z;
        for (Set<BlockPos> set : blocks.values()) {
            set.removeIf(p -> (p.getX() >> 4) == cx && (p.getZ() >> 4) == cz);
        }
    }

    private void onBlockUpdated(BlockUpdateEvent event) {
        BlockPos pos = event.pos();
        for (Set<BlockPos> set : blocks.values()) {
            set.remove(pos);
        }
        checkBlock(pos.getX(), pos.getY(), pos.getZ(), event.state(), ConfigStore.instance.getConfig().blocks.getMap());
    }

    private void scan(final BlockEspConfig config) {
        BlockEventsProcessor.instance.getChunks().thenAcceptAsync(chunks -> {
            for (SnapshotChunk chunk : chunks) {
                scanChunkForBlock(chunk, config);
            }
        }, BlockEventsProcessor.instance.getExecutor());
    }

    private void scanChunkForBlock(SnapshotChunk chunk, BlockEspConfig config) {
        Set<BlockPos> set = blocks.get(config);
        int minY = chunk.getMinY();
        int xc = chunk.getPos().x << 4;
        int zc = chunk.getPos().z << 4;
        for (int x = 0; x < 16; x++) {
            int xw = xc | x;
            for (int z = 0; z < 16; z++) {
                int zw = zc | z;
                int height = chunk.getHeight(x, z);
                for (int y = minY; y <= height; y++) {
                    BlockState state = chunk.getBlockState(x, y, z);
                    Block block = state.getBlock();
                    for (int i = 0; i < config.blocks.size(); i++) {
                        if (block == config.blocks.get(i)) {
                            set.add(new BlockPos(xw, y, zw));
                        }
                    }
                }
            }
        }
    }

    private void checkBlock(int x, int y, int z, BlockState state, Map<Block, BlockEspConfig> map) {
        if (state.isAir()) {
            return;
        }

        BlockEspConfig config = map.get(state.getBlock());
        if (config != null) {
            Set<BlockPos> set = blocks.get(config);
            if (set != null) {
                set.add(new BlockPos(x, y, z));
            }
        }
    }
}