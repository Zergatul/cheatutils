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
    }

    public long distanceSqrTo(int x, int z) {
        long dx = x - pos.getMiddleBlockX();
        long dz = z - pos.getMiddleBlockZ();
        return dx * dx + dz * dz;
    }

    public void markForScanAll() {
        scanAllConfigs = true;
        queuedConfigs = null;
        queuedBlockUpdates = null;
    }

    public void markForScan(BlockEspConfig config) {
        if (scanAllConfigs) {
            return;
        }
        if (queuedConfigs == null) {
            queuedConfigs = new ArrayList<>(2);
        }
        if (!queuedConfigs.contains(config)) {
            queuedConfigs.add(config);
        }
    }

    public void markBlockUpdated(BlockUpdateEvent event) {
        if (scanAllConfigs) {
            return;
        }
        if (queuedBlockUpdates == null) {
            queuedBlockUpdates = new ArrayList<>();
        }
        queuedBlockUpdates.add(event);
    }

    public void markCancelled() {
        cancelled = true;
    }
}