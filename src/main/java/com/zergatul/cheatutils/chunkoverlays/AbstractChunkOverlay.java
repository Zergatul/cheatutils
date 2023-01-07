package com.zergatul.cheatutils.chunkoverlays;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.controllers.ChunkController;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractChunkOverlay {

    protected final Minecraft mc = Minecraft.getInstance();
    protected final int segmentSize;
    // don't update segment often than UpdateDelay ns
    private final long updateDelay;
    private final Map<Dimension, Map<SegmentPos, Segment>> dimensions = new ConcurrentHashMap<>();
    private final Object loopWaitEvent = new Object();
    private final Thread eventLoop;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final Queue<Runnable> endTickQueue = new ConcurrentLinkedQueue<>();
    private final Queue<RenderThreadQueueItem> renderQueue = new ConcurrentLinkedQueue<>();
    private final Set<Segment> updatedSegments = new HashSet<>();
    private final List<Segment> textureUploaded = new ArrayList<>();

    protected AbstractChunkOverlay(int segmentSize, long updateDelay) {
        this.segmentSize = segmentSize;
        this.updateDelay = updateDelay;

        eventLoop = new Thread(this::eventLoopThreadFunc, getThreadName());
        eventLoop.start();
    }

    public final void onEnabledChanged() {
        if (isEnabled()) {
            ChunkController.instance.getLoadedChunks().forEach(p -> onChunkLoaded(p.getFirst(), p.getSecond()));
        } else {
            queue.add(() -> {
                renderQueue.clear();
                endTickQueue.clear();
                queue.clear();

                for (Map<SegmentPos, Segment> segments: dimensions.values()) {
                    for (Segment segment: segments.values()) {
                        segment.close();
                    }
                    segments.clear();
                }
            });
            synchronized (loopWaitEvent) {
                loopWaitEvent.notify();
            }
        }
    }

    public final void onChunkLoaded(Dimension dimension, LevelChunk chunk) {
        if (!isEnabled()) {
            return;
        }

        Map<SegmentPos, Segment> segments = dimensions.computeIfAbsent(dimension, d -> new HashMap<>());

        queue.add(() -> {
            if (!drawChunk(dimension, segments, chunk)) {
                onChunkLoaded(dimension, chunk);
            }
        });

        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    public final void onBlockChanged(Dimension dimension, BlockPos pos, BlockState state) {
        if (!isEnabled()) {
            return;
        }

        var chunkPos = new ChunkPos(pos);
        var segmentPos = new SegmentPos(chunkPos, segmentSize);
        Map<SegmentPos, Segment> segments = dimensions.computeIfAbsent(dimension, d -> new HashMap<>());
        Segment segment = segments.get(segmentPos);
        endTickQueue.add(() -> processBlockChange(dimension, chunkPos, segment, pos, state));
    }

    public final void onClientTickEnd() {
        while (endTickQueue.size() > 0) {
            endTickQueue.remove().run();
        }
    }

    public final void onPreRender() {
        boolean shouldNotify = renderQueue.size() > 0;
        while (renderQueue.size() > 0) {
            RenderThreadQueueItem item = renderQueue.remove();
            item.runnable.run();
            if (item.continuation != null) {
                queue.add(item.continuation);
            }
        }

        if (shouldNotify) {
            synchronized (loopWaitEvent) {
                loopWaitEvent.notify();
            }
        }

        textureUploaded.clear();
        long now = System.nanoTime();
        for (Segment segment: updatedSegments) {
            if (now - segment.updateTime > updateDelay) {
                segment.onChange();
                segment.updated = false;
                segment.updateTime = 0;
                textureUploaded.add(segment);
            }
        }

        for (Segment segment: textureUploaded) {
            updatedSegments.remove(segment);
        }
    }

    public final Collection<Segment> getSegments(Dimension dimension) {
        Map<SegmentPos, Segment> segments = dimensions.computeIfAbsent(dimension, d -> new HashMap<>());
        return segments.values();
    }

    public final int getScanningQueueCount() {
        return queue.size();
    }

    public final String getThreadState() {
        Thread thread = eventLoop;
        if (thread != null) {
            return eventLoop.getState().toString();
        } else {
            return null;
        }
    }

    public abstract int getTranslateZ();

    public abstract boolean isEnabled();

    public void onPostDrawSegments(Dimension dimension, PoseStack poseStack, float xp, float zp, float xc, float zc, float multiplier) {

    }

    protected abstract boolean drawChunk(Dimension dimension, Map<SegmentPos, Segment> segments, LevelChunk chunk);

    protected abstract void processBlockChange(Dimension dimension, ChunkPos chunkPos, Segment segment, BlockPos pos, BlockState state);

    protected final void addToRenderQueue(RenderThreadQueueItem item) {
        renderQueue.add(item);
    }

    protected final void addUpdatedSegment(Segment segment) {
        updatedSegments.add(segment);
    }

    protected abstract String getThreadName();

    private void eventLoopThreadFunc() {
        try {
            while (true) {
                synchronized (loopWaitEvent) {
                    loopWaitEvent.wait();
                }
                while (queue.size() > 0) {
                    queue.remove().run();
                    Thread.yield();
                }
            }
        }
        catch (InterruptedException e) {
            // do nothing
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Segment {
        public final SegmentPos pos;
        public final NativeImage image;
        public final DynamicTexture texture;
        public boolean updated;
        public long updateTime;

        public Segment(SegmentPos pos, int segmentSize) {
            this.pos = pos;
            this.image = new NativeImage(segmentSize * 16, segmentSize * 16, true);
            this.texture = new DynamicTexture(image);
        }

        public void onChange() {
            texture.upload();
        }

        public void close() {
            texture.close();
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof Segment)) {
                return false;
            } else {
                Segment segment = (Segment) obj;
                return this.pos.equals(segment.pos);
            }
        }
    }

    public static class SegmentPos {
        public int x;
        public int z;

        public SegmentPos(ChunkPos pos, int segmentSize) {
            this.x = Math.floorDiv(pos.x, segmentSize);
            this.z = Math.floorDiv(pos.z, segmentSize);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(x, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof SegmentPos pos)) {
                return false;
            } else {
                return this.x == pos.x && this.z == pos.z;
            }
        }
    }

    protected static class RenderThreadQueueItem {
        public Runnable runnable;
        public Runnable continuation;

        public RenderThreadQueueItem(Runnable runnable) {
            this.runnable = runnable;
        }

        public RenderThreadQueueItem(Runnable runnable, Runnable continuation) {
            this.runnable = runnable;
            this.continuation = continuation;
        }
    }
}