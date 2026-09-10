package com.zergatul.cheatutils.modules.esp.blocks;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.SnapshotChunk;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockFinder {

    public static final BlockFinder instance = new BlockFinder();

    public final Map<BlockEspConfig, Set<BlockPos>> blocks = new ConcurrentHashMap<>();

    private BlockFinder() {
        Events.ChunkLoaded.add(this::onChunkLoaded);
        Events.ChunkUnloaded.add(this::onChunkUnloaded);
        Events.BlockUpdated.add(this::onBlockUpdated);
    }

    public void addConfig(BlockEspConfig config) {
        BlockEventsProcessor.instance.getExecutor().execute(() -> {
            blocks.put(config, ConcurrentHashMap.newKeySet());
            scan(config);
        });
    }

    public void applyConfigs(ImmutableList<BlockEspConfig> configs) {
        BlockEventsProcessor.instance.getExecutor().execute(() -> {
            blocks.clear();
            for (BlockEspConfig config : configs) {
                blocks.put(config, ConcurrentHashMap.newKeySet());
            }
            scanAll();
        });
    }

    public void removeConfig(BlockEspConfig config) {
        blocks.remove(config);
    }

    public void clearPositions() {
        for (Set<BlockPos> set : blocks.values()) {
            set.clear();
        }
    }

    public void rescan() {
        BlockEventsProcessor.instance.getExecutor().execute(() -> {
            clearPositions();
            scanAll();
        });
    }

    private void onChunkLoaded(SnapshotChunk chunk) {
        // need to call unload?
        Map<Block, BlockEspConfig> map = ConfigStore.instance.getConfig().blocks.getMap();
        int xc = chunk.getPos().x << 4;
        int zc = chunk.getPos().z << 4;
        for (int x = 0; x < 16; x++) {
            int xw = xc | x;
            for (int z = 0; z < 16; z++) {
                int zw = zc | z;
                int height = chunk.getHeight(x, z);
                for (int y = 0; y < height; y++) {
                    IBlockState state = chunk.getBlockState(x, y, z);
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
        BlockEventsProcessor.instance.requestScan(config);
    }

    private void scanAll() {
        BlockEventsProcessor.instance.requestFullScan();
    }

    public void scanChunkForBlock(SnapshotChunk chunk, BlockEspConfig config) {
        Set<BlockPos> set = blocks.get(config);
        if (set == null) {
            return;
        }

        ImmutableList<Block> blockTypes = config.blocks;
        int xc = chunk.getPos().x << 4;
        int zc = chunk.getPos().z << 4;
        for (int x = 0; x < 16; x++) {
            int xw = xc | x;
            for (int z = 0; z < 16; z++) {
                int zw = zc | z;
                int height = chunk.getHeight(x, z);
                for (int y = 0; y < height; y++) {
                    IBlockState state = chunk.getBlockState(x, y, z);
                    Block block = state.getBlock();
                    for (int i = 0; i < blockTypes.size(); i++) {
                        if (block == blockTypes.get(i)) {
                            set.add(new BlockPos(xw, y, zw));
                        }
                    }
                }
            }
        }
    }

    private void checkBlock(int x, int y, int z, IBlockState state, Map<Block, BlockEspConfig> map) {
        if (state.getMaterial() == Material.AIR) {
            return;
        }

        BlockEspConfig config = map.get(state.getBlock());
        if (config != null) {
            Set<BlockPos> set = blocks.get(config);
            if (set != null) {
                BlockPos pos = new BlockPos(x, y, z);
                set.add(pos);
            }
        }
    }
}