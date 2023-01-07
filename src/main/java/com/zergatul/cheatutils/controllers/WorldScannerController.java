package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.interfaces.LevelChunkMixinInterface;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.BlockUpdateEvent;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldScannerController {

    public static WorldScannerController instance = new WorldScannerController();

    private static final long waitTime = 250000000; // 250ms

    private final Logger logger = LogManager.getLogger(WorldScannerController.class);
    private final Object loopWaitEvent = new Object();
    private Thread eventLoop;
    private Queue<InterruptibleRunnable> queue = new ConcurrentLinkedQueue<>();

    private WorldScannerController() {
        ModApiWrapper.SmartChunkLoaded.add(this::onChunkLoaded);
        ModApiWrapper.SmartChunkUnloaded.add(this::onChunkUnLoaded);
        ModApiWrapper.BlockUpdated.add(this::onBlockChanged);

        restartBackgroundThread();
    }

    private void restartBackgroundThread() {
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
            try {
                while (true) {
                    synchronized (loopWaitEvent) {
                        loopWaitEvent.wait();
                    }
                    while (queue.size() > 0) {
                        InterruptibleRunnable process = queue.remove();
                        process.run();
                        Thread.yield();
                    }
                }
            }
            catch (InterruptedException e) {
                // do nothing
            }
            catch (Throwable e) {
                logger.error("WorldScanner thread crash.", e);
            }
        }, "WorldScannerThread");

        eventLoop.start();
    }

    private void onChunkLoaded(LevelChunk chunk) {
        LevelChunkMixinInterface mixinChunk = (LevelChunkMixinInterface) chunk;
        mixinChunk.onLoad();
        addToQueue(() -> processChunkLoad(chunk));
    }

    private void onChunkUnLoaded(LevelChunk chunk) {
        LevelChunkMixinInterface mixinChunk = (LevelChunkMixinInterface) chunk;
        mixinChunk.onUnload();
        addToQueue(() -> processChunkUnload(chunk));
    }

    private void onBlockChanged(BlockUpdateEvent event) {
        addToQueue(() -> processBlockChanged(event));
    }

    private void processChunkLoad(LevelChunk chunk) throws InterruptedException {
        waitForChunk(chunk, () -> addToQueue(() -> processChunkLoad(chunk)));
        ModApiWrapper.ScannerChunkLoaded.trigger(chunk);
    }

    private void processChunkUnload(LevelChunk chunk) {
        ModApiWrapper.ScannerChunkUnloaded.trigger(chunk);
    }

    private void processBlockChanged(BlockUpdateEvent event) throws InterruptedException {
        waitForChunk(event.chunk(), () -> addToQueue(() -> processBlockChanged(event)));
        ModApiWrapper.ScannerBlockUpdated.trigger(event);
    }

    private void waitForChunk(LevelChunk chunk, Runnable whenNotFull) throws InterruptedException {
        LevelChunkMixinInterface mixinChunk = (LevelChunkMixinInterface) chunk;
        if (chunk.getStatus() != ChunkStatus.FULL) {
            // re-add chunk to the queue
            // but check if this chunk is still valid
            if (!mixinChunk.isUnloaded()) {
                whenNotFull.run();
            }
            return;
        }

        if (mixinChunk.isUnloaded()) {
            return;
        }

        long delta = System.nanoTime() - mixinChunk.getLoadTime();
        if (delta > 0 && delta < waitTime) {
            Thread.sleep((waitTime - delta) / 1000000);
        }
    }

    private void addToQueue(InterruptibleRunnable runnable) {
        queue.add(runnable);
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    @FunctionalInterface
    private interface InterruptibleRunnable {
        void run() throws InterruptedException;
    }
}