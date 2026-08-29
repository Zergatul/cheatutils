package com.zergatul.cheatutils.chunkoverlays;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.controllers.ChunkController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractChunkOverlay {

    protected final Minecraft mc = Minecraft.getInstance();
    protected final int segmentSize;
    // don't update segment often than UpdateDelay ns
    private final long updateDelay;
    private final Map<Dimension, Map<SegmentPos, Segment>> dimensions = new ConcurrentHashMap<>();
    private final Set<Segment> updatedSegments = new HashSet<>();
    private final List<Segment> textureUploaded = new ArrayList<>();
    private volatile boolean closed;

    protected AbstractChunkOverlay(int segmentSize, long updateDelay) {
        this.segmentSize = segmentSize;
        this.updateDelay = updateDelay;

    }

    public final void onEnabledChanged() {
        ClientTickEndExecutor.instance.execute(() -> {
            if (closed) {
                return;
            }
            if (isEnabled()) {
                ChunkController.instance.getLoadedChunks().forEach(p -> onChunkLoaded(p.getSecond()));
            } else {
                closeSegments();
            }
        });
    }

    public final void onChunkLoaded(LevelChunk chunk) {
        if (closed || !isEnabled()) {
            return;
        }

        Dimension dimension = Dimension.get((ClientLevel) chunk.getLevel());
        Map<SegmentPos, Segment> segments = getSegmentsMap(dimension);
        drawChunk(dimension, segments, chunk);
    }

    public final void onBlockChanged(Dimension dimension, BlockPos pos, BlockState state) {
        if (closed || !isEnabled()) {
            return;
        }

        var chunkPos = new ChunkPos(pos);
        var segmentPos = new SegmentPos(chunkPos, segmentSize);
        Map<SegmentPos, Segment> segments = dimensions.computeIfAbsent(dimension, d -> new HashMap<>());
        Segment segment = segments.get(segmentPos);
        processBlockChange(dimension, chunkPos, segment, pos, state);
    }

    public final void onPreRender() {
        if (closed) {
            return;
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
        return getSegmentsMap(dimension).values();
    }

    public abstract int getTranslateZ();

    public abstract boolean isEnabled();

    public void onPostDrawSegments(Dimension dimension, PoseStack poseStack, float xp, float zp, float xc, float zc, float multiplier) {

    }

    protected final Map<SegmentPos, Segment> getSegmentsMap(Dimension dimension) {
        return dimensions.computeIfAbsent(dimension, d -> new HashMap<>());
    }

    protected abstract boolean drawChunk(Dimension dimension, Map<SegmentPos, Segment> segments, LevelChunk chunk);

    protected abstract void processBlockChange(Dimension dimension, ChunkPos chunkPos, Segment segment, BlockPos pos, BlockState state);

    protected final void addUpdatedSegment(Segment segment) {
        updatedSegments.add(segment);
    }

    public final void close() {
        closed = true;
        closeSegments();
    }

    private void closeSegments() {
        updatedSegments.clear();
        textureUploaded.clear();
        for (Map<SegmentPos, Segment> segments : dimensions.values()) {
            for (Segment segment : segments.values()) {
                segment.close();
            }
            segments.clear();
        }
        dimensions.clear();
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

}