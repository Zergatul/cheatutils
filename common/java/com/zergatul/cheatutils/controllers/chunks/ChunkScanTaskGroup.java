package com.zergatul.cheatutils.controllers.chunks;

import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class ChunkScanTaskGroup {

    public final ChunkPos pos;

    public boolean scanAllConfigs;
    public @Nullable List<BlockEspConfig> queuedConfigs;
    public @Nullable List<BlockUpdateEvent> queuedBlockUpdates;
    public boolean cancelled;

    public ChunkScanTaskGroup(ChunkPos pos) {
        this.pos = pos;
        this.scanAllConfigs = false;
        this.queuedConfigs = null;
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
        this.queuedConfigs = null;
        this.queuedBlockUpdates = null;
    }

    public void markForScan(BlockEspConfig config) {
        if (this.scanAllConfigs) {
            // scanning all configs requested, no need to scan individual
            return;
        }

        if (this.queuedConfigs == null) {
            this.queuedConfigs = new ArrayList<>(2);
        }
        this.queuedConfigs.add(config);
    }

    public void markBlockUpdated(BlockUpdateEvent event) {
        if (this.scanAllConfigs) {
            // scanning all block requested, scan will take copy of chunk with all updates
            return;
        }

        if (this.queuedBlockUpdates == null) {
            this.queuedBlockUpdates = new ArrayList<>();
            this.queuedBlockUpdates.add(event);
        }
    }

    public void markCancelled() {
        this.cancelled = true;
    }
}