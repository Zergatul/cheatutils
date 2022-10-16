package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.utils.ThreadLoadCounter;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.Heightmap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockFinderController {

    public static final BlockFinderController instance = new BlockFinderController();

    public final HashMap<ResourceLocation, HashSet<BlockPos>> blocks = new HashMap<>();

    private Minecraft mc = Minecraft.getInstance();
    private final Object loopWaitEvent = new Object();
    private Thread eventLoop;
    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private ThreadLoadCounter counter = new ThreadLoadCounter();

    private BlockFinderController() {
        ChunkController.instance.addOnChunkLoadedHandler(this::scanChunk);
        ChunkController.instance.addOnChunkUnLoadedHandler(this::unloadChunk);
        ChunkController.instance.addOnBlockChangedHandler(this::handleBlockUpdate);
        start();
    }

    public void start() {
        stop();

        eventLoop = new Thread(() -> {
            try {
                while (true) {
                    counter.startWait();
                    synchronized (loopWaitEvent) {
                        loopWaitEvent.wait();
                    }
                    counter.startLoad();
                    while (queue.size() > 0) {
                        Runnable process = queue.remove();
                        process.run();
                        Thread.yield();
                    }
                }
            }
            catch (InterruptedException e) {
                // do nothing
            }
            finally {
                counter.dispose();
            }
        });

        eventLoop.start();
    }

    public void stop() {
        if (eventLoop != null) {
            queue.clear();
            synchronized (loopWaitEvent) {
                loopWaitEvent.notify();
            }
            eventLoop.interrupt();
        }

        eventLoop = null;
    }

    public void clear() {
        synchronized (blocks) {
            for (HashSet<BlockPos> set : blocks.values()) {
                set.clear();
            }
        }
    }

    public double getScanningThreadLoadPercent() {
        return 100d * counter.getLoad(1);
    }

    public int getScanningQueueCount() {
        return queue.size();
    }

    private void scanChunk(Dimension dimension, Chunk chunk) {
        //logger.debug("Adding scan chunk {}", chunk.getPos());
        queue.add(() -> {
            while (chunk.getStatus() != ChunkStatus.FULL) {
                // TODO: check better way
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int minY = dimension.getMinY();
            int xc = chunk.getPos().x << 4;
            int zc = chunk.getPos().z << 4;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = chunk.getHeight(Heightmap.Type.WORLD_SURFACE, x, z);
                    for (int y = minY; y <= height; y++) {
                        int xb = xc | x;
                        int zb = zc | z;
                        BlockPos pos = new BlockPos(xb, y, zb);
                        BlockState state = chunk.getBlockState(pos);
                        checkBlock(state, pos);
                    }
                }
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void unloadChunk(Dimension dimension, Chunk chunk) {
        //logger.debug("Adding unload chunk {}", chunk.getPos());
        queue.add(() -> {
            int cx = chunk.getPos().x;
            int cz = chunk.getPos().z;
            synchronized (blocks) {
                for (HashSet<BlockPos> set : blocks.values()) {
                    set.removeIf(pos -> (pos.getX() >> 4) == cx && (pos.getZ() >> 4) == cz);
                }
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void handleBlockUpdate(Dimension dimension, BlockPos pos, BlockState state) {
        queue.add(() -> {
            synchronized (blocks) {
                for (HashSet<BlockPos> set : blocks.values()) {
                    set.remove(pos);
                }
            }
            checkBlock(state, pos);
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    public void scan(BlockTracerConfig config) {
        ResourceLocation id = ModApiWrapper.BLOCKS.getKey(config.block);

        synchronized (blocks) {
            if (blocks.containsKey(id)) {
                blocks.get(id).clear();
            }
        }

        for (Pair<Dimension, Chunk> pair : ChunkController.instance.getLoadedChunks()) {
            scanChunkForBlock(pair.getSecond(), id);
            //logger.debug("Queued scan for block {} in chunk {}", id, chunk.getPos());
        }
    }

    private void scanChunkForBlock(Chunk chunk, ResourceLocation id) {
        queue.add(() -> {
            //logger.debug("Scanning for block {} in chunk {}", id, chunk.getPos());
            int xc = chunk.getPos().x << 4;
            int zc = chunk.getPos().z << 4;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = chunk.getHeight(Heightmap.Type.WORLD_SURFACE, x, z);
                    for (int y = -64; y <= height; y++) {
                        int xb = xc | x;
                        int zb = zc | z;
                        BlockPos pos = new BlockPos(xb, y, zb);
                        BlockState state = chunk.getBlockState(pos);
                        if (ModApiWrapper.BLOCKS.getKey(state.getBlock()).equals(id)) {
                            synchronized (blocks) {
                                if (blocks.containsKey(id)) {
                                    blocks.get(id).add(pos);
                                }
                            }
                        }
                    }
                }
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void checkBlock(BlockState state, BlockPos pos) {

        if (state.getBlock() == Blocks.AIR) {
            return;
        }

        List<BlockTracerConfig> list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {
            for (BlockTracerConfig config: list) {
                ResourceLocation id = ModApiWrapper.BLOCKS.getKey(state.getBlock());
                if (ModApiWrapper.BLOCKS.getKey(config.block).equals(id)) {
                    synchronized (blocks) {
                        if (blocks.containsKey(id)) {
                            blocks.get(id).add(pos);
                        }
                    }
                }
            }
        }

    }

    private void removeBlock(BlockState state, BlockPos pos) {
        List<BlockTracerConfig> list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {
            for (BlockTracerConfig config: list) {
                ResourceLocation id = ModApiWrapper.BLOCKS.getKey(state.getBlock());
                if (ModApiWrapper.BLOCKS.getKey(config.block).equals(id)) {
                    synchronized (blocks) {
                        if (blocks.containsKey(id)) {
                            blocks.get(id).remove(pos);
                        }
                    }
                }
            }
        }
    }
}