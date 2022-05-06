package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LightLevelController {

    public static final LightLevelController instance = new LightLevelController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Object loopWaitEvent = new Object();
    private final Thread eventLoop;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final HashMap<ChunkPos, HashSet<BlockPos>> chunks = new HashMap<>();
    private final List<BlockPos> listForRendering = new ArrayList<>();
    private boolean active = false;

    private LightLevelController() {

        ChunkController.instance.addOnChunkLoadedHandler(this::onChunkLoaded);
        ChunkController.instance.addOnChunkUnLoadedHandler(this::onChunkUnLoaded);
        ChunkController.instance.addOnBlockChangedHandler(this::onBlockChanged);

        eventLoop = new Thread(() -> {
            try {
                while (true) {
                    synchronized (loopWaitEvent) {
                        loopWaitEvent.wait();
                    }
                    while (queue.size() > 0) {
                        Runnable process = queue.remove();
                        process.run();
                        Thread.sleep(5);
                    }
                }
            }
            catch (InterruptedException e) {
                // do nothing
            }
        });

        eventLoop.start();
    }

    public void setActive(boolean value) {
        if (active != value) {
            active = value;
            if (active) {
                for (ChunkAccess chunk : ChunkController.instance.getLoadedChunks()) {
                    onChunkLoaded(chunk);
                }
            } else {
                queue.clear();
            }
        }
    }

    public List<BlockPos> getBlockForRendering() {
        listForRendering.clear();
        synchronized (chunks) {
            for (HashSet<BlockPos> set : chunks.values()) {
                synchronized (set) {
                    listForRendering.addAll(set);
                }
            }
        }
        return listForRendering;
    }

    private void onChunkLoaded(ChunkAccess chunk) {
        if (!active) {
            return;
        }
        queue.add(() -> {
            ChunkPos chunkPos = chunk.getPos();
            HashSet<BlockPos> set;
            synchronized (chunks) {
                set = chunks.get(chunkPos);
                if (set == null) {
                    set = new HashSet<>();
                    chunks.put(chunkPos, set);
                }
            }
            // TODO: height map???
            //int[] heightMap = chunk.getHeight();
            int xc = chunk.getPos().x << 4;
            int zc = chunk.getPos().z << 4;
            synchronized (set) {
                set.clear();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int height = 255; //heightMap[z << 4 | x];
                        for (int y = 0; y <= height; y++) {
                            int xb = xc | x;
                            int zb = zc | z;
                            BlockPos pos = new BlockPos(xb, y, zb);
                            checkBlock(chunk, pos, set);
                        }
                    }
                }
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void onChunkUnLoaded(ChunkAccess chunk) {
        if (!active) {
            return;
        }
        queue.add(() -> {
            synchronized (chunks) {
                chunks.remove(chunk.getPos());
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void onBlockChanged(BlockPos pos, BlockState state) {
        if (!active) {
            return;
        }
        queue.add(() -> {
            ChunkPos chunkPos = new ChunkPos(pos);
            HashSet<BlockPos> set;
            synchronized (chunks) {
                set = chunks.get(chunkPos);
            }
            if (set == null) {
                return;
            }
            ChunkAccess chunk = mc.level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false);
            if (chunk == null) {
                return;
            }
            synchronized (set) {
                BlockPos above = pos.above();
                BlockPos below = pos.below();
                BlockPos below2 = below.below();
                set.remove(pos);
                set.remove(above);
                set.remove(below);
                set.remove(below2);
                checkBlock(chunk, pos, set);
                checkBlock(chunk, above, set);
                checkBlock(chunk, below, set);
                checkBlock(chunk, below2, set);
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void checkBlock(ChunkAccess chunk, BlockPos pos, HashSet<BlockPos> set) {
        BlockState state = chunk.getBlockState(pos);
        if (state.getMaterial().isSolid() && state.isCollisionShapeFullBlock(mc.level, pos)) {
            BlockPos posAbove = pos.above();
            BlockState stateAbove = chunk.getBlockState(posAbove);
            if (stateAbove.getMaterial().isSolid()) {
                return;
            }
            if (!stateAbove.getFluidState().isEmpty()) {
                return;
            }
            if (stateAbove.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
                return;
            }

            set.add(posAbove);
        }
    }

}
