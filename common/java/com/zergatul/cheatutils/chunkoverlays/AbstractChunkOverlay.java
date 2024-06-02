package com.zergatul.cheatutils.chunkoverlays;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

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
                AtomicReferenceArray<LevelChunk> chunks = BlockEventsProcessor.instance.getRawChunks();
                for (int i = 0; i < chunks.length(); i++) {
                    LevelChunk chunk = chunks.get(i);
                    if (chunk == null) {
                        continue;
                    }
                    onChunkLoaded(chunk);
                }
            } else {
                for (Map<SegmentPos, Segment> segments : dimensions.values()) {
                    for (Segment segment: segments.values()) {
                        segment.close();
                    }
                    segments.clear();
                }
            }
        });
    }

    public final void onChunkLoaded(LevelChunk chunk) {
        if (!isEnabled()) {
            return;
        }

        Dimension dimension = Dimension.get((ClientLevel) chunk.getLevel());
        Map<SegmentPos, Segment> segments = getSegmentsMap(dimension);
        drawChunk(dimension, chunk, segments);
    }

    public final void onBlockChanged(Dimension dimension, BlockPos pos, BlockState state) {
        if (!isEnabled()) {
            return;
        }

        var chunkPos = new ChunkPos(pos);
        var segmentPos = new SegmentPos(chunkPos, segmentSize);
        Map<SegmentPos, Segment> segments = getSegmentsMap(dimension);
        Segment segment = segments.get(segmentPos);
        processBlockChange(dimension, chunkPos, segment, pos, state);
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
        return dimensions.computeIfAbsent(dimension, d -> new ConcurrentHashMap<>());
    }

    protected void drawChunk(Dimension dimension, LevelChunk chunk, Map<SegmentPos, Segment> segments) {}

    protected void processBlockChange(Dimension dimension, ChunkPos chunkPos, Segment segment, BlockPos pos, BlockState state) {}

    protected final void addUpdatedSegment(Segment segment) {
        updatedSegments.add(segment);
    }

    protected NativeImage loadImage(String filename) {
        ClassLoader classLoader = NewChunksOverlay.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(filename);
        try {
            if (stream == null) {
                throw new IllegalStateException("Stream is null.");
            }
            return NativeImage.read(stream);
        }
        catch (IOException e) {
            ModMain.LOGGER.error(String.format("Cannot load image %s", filename), e);
            return null;
        }
    }
}