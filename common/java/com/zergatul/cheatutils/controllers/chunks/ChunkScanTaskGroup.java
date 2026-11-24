package com.zergatul.cheatutils.controllers.chunks;

import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class ChunkScanTaskGroup {

    public final ChunkPos pos;

    public boolean scanAllConfigs;
    public @Nullable List<Block> queuedBlocksForScan;
    public @Nullable List<BlockUpdateEvent> queuedBlockUpdates;
    public boolean cancelled;

    public ChunkScanTaskGroup(ChunkPos pos) {
        this.pos = pos;
        this.scanAllConfigs = false;
        this.queuedBlocksForScan = null;
        this.queuedBlockUpdates = null;
    }

    public long distanceSqrTo(int x, int z) {
        long dx = x - pos.getMiddleBlockX();
        long dz = z - pos.getMiddleBlockZ();
        return dx * dx + dz * dz;
    }

    public void markForScanAll() {
        // discard scanning separate configs, scan all will scan all blocks at once
        // discard block updates, since new scan will receive snapshot copy of the chunk with all updates already processed
        this.scanAllConfigs = true;
        this.queuedBlocksForScan = null;
        this.queuedBlockUpdates = null;
    }

    public void markForScan(List<Block> blocks) {
        if (this.scanAllConfigs) {
            // scanning all configs requested, no need to scan individual
            return;
        }

        if (this.queuedBlocksForScan == null) {
            this.queuedBlocksForScan = new ArrayList<>(4);
        }

        for (Block block : blocks) {
            // it should be very hard to queue the same config twice, but just in case we will not allow this
            if (!this.queuedBlocksForScan.contains(block)) {
                this.queuedBlocksForScan.add(block);
            }
        }
    }

    public void markBlockUpdated(BlockUpdateEvent event) {
        if (this.scanAllConfigs) {
            // scanning all block requested, scan will take copy of chunk with all updates
            return;
        }

        if (this.queuedBlockUpdates == null) {
            this.queuedBlockUpdates = new ArrayList<>();
        }
        this.queuedBlockUpdates.add(event);
    }

    public void markCancelled() {
        this.cancelled = true;
    }
}