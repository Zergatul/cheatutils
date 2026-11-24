package com.zergatul.cheatutils.modules.esp.block;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import com.zergatul.cheatutils.controllers.SnapshotChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class BlockFinder {

    public static final BlockFinder INSTANCE = new BlockFinder();

    private final Map<Block, Set<BlockPos>> map = new ConcurrentHashMap<>();
    private final Set<BlockPos> blackList = ConcurrentHashMap.newKeySet();

    private BlockFinder() {
        Events.ChunkLoaded.add(this::onChunkLoaded);
        Events.ChunkUnloaded.add(this::onChunkUnloaded);
        Events.BlockUpdated.add(this::onBlockUpdated);
    }

    public @Nullable Set<BlockPos> getPositons(Block block) {
        return map.get(block);
    }

    public void trackBlocks(Set<Block> blocks) {
        BlockEventsProcessor.INSTANCE.getExecutor().execute(() -> {
            List<Block> added = new ArrayList<>();
            for (Block block : blocks) {
                if (!map.containsKey(block)) {
                    added.add(block);
                    map.put(block, ConcurrentHashMap.newKeySet());
                }
            }

            if (!added.isEmpty()) {
                BlockEventsProcessor.INSTANCE.requestScan(added);
            }

            List<Block> removed = new ArrayList<>();
            for (Block block : map.keySet()) {
                if (!blocks.contains(block)) {
                    removed.add(block);
                }
            }

            for (Block block : removed) {
                map.remove(block);
            }
        });
    }

    public void clearPositions() {
        for (Set<BlockPos> set : map.values()) {
            set.clear();
        }
    }

    public void rescan() {
        BlockEventsProcessor.INSTANCE.getExecutor().execute(() -> {
            clearPositions();
            scanAll();
        });
    }

    public void addBlackList(BlockPos pos) {
        blackList.add(pos);
        for (Set<BlockPos> set : map.values()) {
            set.remove(pos);
        }
    }

    public void clearBlackList() {
        blackList.clear();
    }

    private void onChunkLoaded(SnapshotChunk chunk) {
        // need to call unload?
        int minY = chunk.getMinY();
        int xc = chunk.getPos().x() << 4;
        int zc = chunk.getPos().z() << 4;
        for (int x = 0; x < 16; x++) {
            int xw = xc | x;
            for (int z = 0; z < 16; z++) {
                int zw = zc | z;
                int height = minY + chunk.getHeight(x, z);
                for (int y = minY; y < height; y++) {
                    BlockState state = chunk.getBlockState(x, y, z);
                    checkBlock(xw, y, zw, state);
                }
            }
        }
    }

    private void onChunkUnloaded(ChunkPos pos) {
        final int cx = pos.x();
        final int cz = pos.z();
        for (Set<BlockPos> set : map.values()) {
            set.removeIf(p -> (p.getX() >> 4) == cx && (p.getZ() >> 4) == cz);
        }
    }

    private void onBlockUpdated(BlockUpdateEvent event) {
        BlockPos pos = event.pos();
        for (Set<BlockPos> set : map.values()) {
            set.remove(pos);
        }
        checkBlock(pos.getX(), pos.getY(), pos.getZ(), event.state());
    }

    private void scanAll() {
        BlockEventsProcessor.INSTANCE.requestFullScan();
    }

    public void scanChunkForBlocks(SnapshotChunk chunk, List<Block> blocks) {
        Collection<Block> set = blocks.size() <= 4 ? blocks : new HashSet<>(blocks);

        int minY = chunk.getMinY();
        int xc = chunk.getPos().x() << 4;
        int zc = chunk.getPos().z() << 4;
        for (int x = 0; x < 16; x++) {
            int xw = xc | x;
            for (int z = 0; z < 16; z++) {
                int zw = zc | z;
                int height = chunk.getHeight(x, z) + minY;
                for (int y = minY; y < height; y++) {
                    BlockState state = chunk.getBlockState(x, y, z);
                    Block block = state.getBlock();
                    if (set.contains(block)) {
                        checkBlock(xw, y, zw, state);
                    }
                }
            }
        }
    }

    private void checkBlock(int x, int y, int z, BlockState state) {
        if (state.isAir()) {
            return;
        }

        Set<BlockPos> positions = map.get(state.getBlock());
        if (positions == null) {
            return;
        }

        BlockPos pos = new BlockPos(x, y, z);
        if (!blackList.contains(pos)) {
            positions.add(pos);
        }
    }
}