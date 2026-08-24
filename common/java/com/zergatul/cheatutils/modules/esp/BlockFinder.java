package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import com.zergatul.cheatutils.controllers.SnapshotChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockFinder {

    public static final BlockFinder instance = new BlockFinder();

    public final Map<BlockEspConfig, Set<BlockPos>> blocks = new ConcurrentHashMap<>();

    private BlockFinder() {
        Events.SnapshotChunkLoaded.add(this::onChunkLoaded);
        Events.SnapshotChunkUnloaded.add(this::onChunkUnloaded);
        Events.SnapshotBlockUpdated.add(this::onBlockUpdated);
    }

    public void addConfig(BlockEspConfig config) {
        BlockEventsProcessor.instance.getExecutor().execute(() -> {
            blocks.put(config, ConcurrentHashMap.newKeySet());
            BlockEventsProcessor.instance.requestScan(config);
        });
    }

    public void applyConfigs(ImmutableList<BlockEspConfig> configs) {
        BlockEventsProcessor.instance.getExecutor().execute(() -> {
            blocks.clear();
            for (BlockEspConfig config : configs) {
                blocks.put(config, ConcurrentHashMap.newKeySet());
            }
            BlockEventsProcessor.instance.requestFullScan();
        });
    }

    public void removeConfig(BlockEspConfig config) {
        blocks.remove(config);
    }

    public void removeAllConfigs() {
        blocks.clear();
    }

    public void clear() {
        BlockEventsProcessor.instance.getExecutor().execute(this::clearPositions);
    }

    public void clearPositions() {
        for (Set<BlockPos> set : blocks.values()) {
            set.clear();
        }
    }

    public void restart() {
        rescan();
    }

    public void rescan() {
        BlockEventsProcessor.instance.getExecutor().execute(() -> {
            clearPositions();
            BlockEventsProcessor.instance.requestFullScan();
        });
    }

    private void onChunkLoaded(SnapshotChunk chunk) {
        Map<Block, BlockEspConfig> map = ConfigStore.instance.getConfig().blocks.getMap();
        int minY = chunk.getMinY();
        int chunkX = chunk.getPos().x << 4;
        int chunkZ = chunk.getPos().z << 4;
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX | x;
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ | z;
                int height = minY + chunk.getHeight(x, z);
                for (int y = minY; y < height; y++) {
                    checkBlock(worldX, y, worldZ, chunk.getBlockState(x, y, z), map);
                }
            }
        }
    }

    private void onChunkUnloaded(ChunkPos pos) {
        int chunkX = pos.x;
        int chunkZ = pos.z;
        for (Set<BlockPos> set : blocks.values()) {
            set.removeIf(blockPos ->
                    (blockPos.getX() >> 4) == chunkX && (blockPos.getZ() >> 4) == chunkZ);
        }
    }

    private void onBlockUpdated(BlockUpdateEvent event) {
        BlockPos pos = event.pos();
        for (Set<BlockPos> set : blocks.values()) {
            set.remove(pos);
        }
        checkBlock(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                event.state(),
                ConfigStore.instance.getConfig().blocks.getMap());
    }

    public void scanChunkForBlock(SnapshotChunk chunk, BlockEspConfig config) {
        Set<BlockPos> set = blocks.get(config);
        if (set == null) {
            return;
        }

        ImmutableList<Block> blockTypes = config.blocks;
        int minY = chunk.getMinY();
        int chunkX = chunk.getPos().x << 4;
        int chunkZ = chunk.getPos().z << 4;
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX | x;
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ | z;
                int height = minY + chunk.getHeight(x, z);
                for (int y = minY; y < height; y++) {
                    Block block = chunk.getBlockState(x, y, z).getBlock();
                    for (int i = 0; i < blockTypes.size(); i++) {
                        if (block == blockTypes.get(i)) {
                            set.add(new BlockPos(worldX, y, worldZ));
                            break;
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