package com.zergatul.cheatutils.chunkoverlays;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.concurrent.PreRenderGuiExecutor;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import com.zergatul.cheatutils.controllers.SnapshotChunk;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractChunkOverlay {

    protected final Minecraft mc = Minecraft.getInstance();
    protected final int segmentSize;
    // don't update segment often than UpdateDelay ns
    private final long updateDelay;
    private final Map<Dimension, Map<SegmentPos, Segment>> dimensions = new ConcurrentHashMap<>();
    private final Set<Segment> updatedSegments = new HashSet<>();
    private final List<Segment> textureUploaded = new ArrayList<>();

    protected AbstractChunkOverlay(int segmentSize, long updateDelay) {
        this.segmentSize = segmentSize;
        this.updateDelay = updateDelay;
    }

    public final void onEnabledChanged() {
        TickEndExecutor.instance.execute(() -> {
            if (isEnabled()) {
                BlockEventsProcessor.instance.getChunks().thenAcceptAsync(chunks -> {
                    for (SnapshotChunk chunk : chunks) {
                        onChunkLoaded(chunk);
                    }
                }, PreRenderGuiExecutor.instance);
            } else {
                PreRenderGuiExecutor.instance.execute(() -> {
                    for (Map<SegmentPos, Segment> segments: dimensions.values()) {
                        for (Segment segment: segments.values()) {
                            segment.close();
                        }
                        segments.clear();
                    }
                });
            }
        });
    }

    public final void onChunkLoaded(SnapshotChunk chunk) {
        if (!isEnabled()) {
            return;
        }

        Map<SegmentPos, Segment> segments = getSegmentsMap(chunk.getDimension());
        drawChunk(segments, chunk);
    }

    public final void onBlockChanged(Dimension dimension, BlockPos pos, BlockState state) {
        TickEndExecutor.instance.execute(() -> {
            if (!isEnabled()) {
                return;
            }

            var chunkPos = new ChunkPos(pos);
            var segmentPos = new SegmentPos(chunkPos, segmentSize);
            Map<SegmentPos, Segment> segments = dimensions.computeIfAbsent(dimension, d -> new HashMap<>());
            Segment segment = segments.get(segmentPos);
            processBlockChange(dimension, chunkPos, segment, pos, state);
        });
    }

    public final void onPreRender() {
        textureUploaded.clear();
        long now = System.nanoTime();
        for (Segment segment : updatedSegments) {
            if (now - segment.updateTime > updateDelay) {
                segment.onChange();
                segment.updated = false;
                segment.updateTime = 0;
                textureUploaded.add(segment);
            }
        }

        for (Segment segment : textureUploaded) {
            updatedSegments.remove(segment);
        }
    }

    public final Collection<Segment> getSegments(Dimension dimension) {
        return getSegmentsMap(dimension).values();
    }

    public abstract int getTranslateZ();

    public abstract boolean isEnabled();

    public void onPostDrawSegments(Dimension dimension, PoseStack poseStack, float xp, float zp, float xc, float zc, float multiplier) {

    }

    protected final Map<SegmentPos, Segment> getSegmentsMap(Dimension dimension) {
        return dimensions.computeIfAbsent(dimension, d -> new HashMap<>());
    }

    protected void drawChunk(Map<SegmentPos, Segment> segments, SnapshotChunk chunk) {}

    protected void processBlockChange(Dimension dimension, ChunkPos chunkPos, Segment segment, BlockPos pos, BlockState state) {}

    protected final void addToRenderQueue(RenderThreadQueueItem item) {
        if (item.continuation != null) {
            CompletableFuture
                    .runAsync(item.runnable, PreRenderGuiExecutor.instance)
                    .thenRunAsync(item.continuation, BlockEventsProcessor.instance.getExecutor());
        } else {
            PreRenderGuiExecutor.instance.execute(item.runnable);
        }
    }

    protected final void addUpdatedSegment(Segment segment) {
        updatedSegments.add(segment);
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
            } else if (!(obj instanceof Segment segment)) {
                return false;
            } else {
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