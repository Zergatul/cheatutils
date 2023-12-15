package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.utils.ThreadLoadCounter;
import com.zergatul.cheatutils.interfaces.LevelChunkMixinInterface;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockFinderController {

    public static final BlockFinderController instance = new BlockFinderController();

    // all modification to blocks are done in eventLoop thread
    public final Map<BlockEspConfig, Set<BlockPos>> blocks = new ConcurrentHashMap<>();

    private final Logger logger = LogManager.getLogger(BlockFinderController.class);
    private final Object loopWaitEvent = new Object();
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final ThreadLoadCounter counter = new ThreadLoadCounter();
    private Thread eventLoop;

    private BlockFinderController() {
        Events.ScannerChunkLoaded.add(this::scanChunk);
        Events.ScannerChunkUnloaded.add(this::unloadChunk);
        Events.ScannerBlockUpdated.add(this::handleBlockUpdate);

        restartBackgroundThread(null);
    }

    public void restart() {
        restartBackgroundThread(() -> {
            clear();
            for (Pair<Dimension, LevelChunk> pair: ChunkController.instance.getLoadedChunks()) {
                scanChunk(pair.getSecond());
            }
        });
    }

    private void restartBackgroundThread(Runnable beforeThreadStart) {
        /* stop */
        if (eventLoop != null) {
            queue.clear();
            synchronized (loopWaitEvent) {
                loopWaitEvent.notify();
            }
            eventLoop.interrupt();
        }

        eventLoop = null;

        /* start */

        eventLoop = new Thread(() -> {
            boolean first = true;
            try {
                while (true) {
                    counter.startWait();
                    if (first) {
                        first = false;
                    } else {
                        synchronized (loopWaitEvent) {
                            loopWaitEvent.wait();
                        }
                    }
                    counter.startLoad();
                    while (!queue.isEmpty()) {
                        Runnable process = queue.remove();
                        process.run();
                        Thread.yield();
                    }
                }
            }
            catch (InterruptedException e) {
                // do nothing
            }
            catch (Throwable e) {
                logger.error("BlockFinder scan thread crash.", e);
            }
            finally {
                counter.dispose();
            }
        }, "BlockFinderScanThread");

        if (beforeThreadStart != null) {
            beforeThreadStart.run();
        }

        eventLoop.start();
    }

    public void clear() {
        addToQueue(() -> {
            for (BlockEspConfig config: blocks.keySet()) {
                blocks.put(config, ConcurrentHashMap.newKeySet());
            }
        });
    }

    public void addConfig(BlockEspConfig config) {
        addToQueue(() -> {
            blocks.put(config, ConcurrentHashMap.newKeySet());
            scan(config);
        });
    }

    public void removeConfig(BlockEspConfig config) {
        addToQueue(() -> blocks.remove(config));
    }

    public void removeAllConfigs() {
        addToQueue(blocks::clear);
    }

    public String getThreadState() {
        Thread thread = eventLoop;
        if (thread != null) {
            return eventLoop.getState().toString();
        } else {
            return null;
        }
    }

    public double getScanningThreadLoadPercent() {
        return 100d * counter.getLoad(1);
    }

    public int getScanningQueueCount() {
        return queue.size();
    }

    private void scanChunk(LevelChunk chunk) {
        addToQueue(() -> {
            Map<Block, BlockEspConfig> map = ConfigStore.instance.getConfig().blocks.getMap();
            LevelChunkMixinInterface mixinChunk = (LevelChunkMixinInterface) chunk;
            int minY = mixinChunk.getDimension().getMinY();
            int xc = chunk.getPos().x << 4;
            int zc = chunk.getPos().z << 4;
            var pos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 16; x++) {
                pos.setX(xc | x);
                for (int z = 0; z < 16; z++) {
                    pos.setZ(zc | z);
                    int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                    for (int y = minY; y <= height; y++) {
                        pos.setY(y);
                        BlockState state = chunk.getBlockState(pos);
                        checkBlock(state, pos, map);
                    }
                }
            }
        });
    }

    private void unloadChunk(LevelChunk chunk) {
        addToQueue(() -> {
            int cx = chunk.getPos().x;
            int cz = chunk.getPos().z;
            for (Set<BlockPos> set: blocks.values()) {
                set.removeIf(pos -> (pos.getX() >> 4) == cx && (pos.getZ() >> 4) == cz);
            }
        });
    }

    private void handleBlockUpdate(BlockUpdateEvent event) {
        addToQueue(() -> {
            for (Set<BlockPos> set: blocks.values()) {
                set.remove(event.pos());
            }
            checkBlock(event.state(), event.pos(), ConfigStore.instance.getConfig().blocks.getMap());
        });
    }

    private void scan(BlockEspConfig config) {
        for (Pair<Dimension, LevelChunk> pair: ChunkController.instance.getLoadedChunks()) {
            scanChunkForBlock(pair.getFirst(), pair.getSecond(), config);
        }
    }

    private void scanChunkForBlock(Dimension dimension, ChunkAccess chunk, BlockEspConfig config) {
        Set<BlockPos> set = blocks.get(config);
        int minY = dimension.getMinY();
        int xc = chunk.getPos().x << 4;
        int zc = chunk.getPos().z << 4;
        var pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            pos.setX(xc | x);
            for (int z = 0; z < 16; z++) {
                pos.setZ(zc | z);
                int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                for (int y = minY; y <= height; y++) {
                    pos.setY(y);
                    BlockState state = chunk.getBlockState(pos);
                    Block block = state.getBlock();
                    for (int i = 0; i < config.blocks.size(); i++) {
                        if (block == config.blocks.get(i)) {
                            set.add(pos.immutable());
                        }
                    }
                }
            }
        }
    }

    private void checkBlock(BlockState state, BlockPos pos, Map<Block, BlockEspConfig> map) {
        if (state.isAir()) {
            return;
        }

        BlockEspConfig config = map.get(state.getBlock());
        if (config != null) {
            Set<BlockPos> set = blocks.get(config);
            if (set != null) {
                set.add(pos.immutable());
            }
        }
    }

    private void addToQueue(Runnable runnable) {
        queue.add(runnable);
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }
}