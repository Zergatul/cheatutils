package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class NewChunksController {

    public static final NewChunksController instance = new NewChunksController();

    private static final Color New = Color.GREEN;
    private static final Color Old = Color.RED;
    private static final Color SemiNew = Color.YELLOW;

    private final Logger logger = LogManager.getLogger(NewChunksController.class);
    private final Minecraft mc = Minecraft.getInstance();
    private final Thread eventLoop;
    private final Object loopWaitEvent = new Object();
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final Map<Dimension, Map<ChunkPos, ChunkEntry>> dimensions = new ConcurrentHashMap<>();

    private NewChunksController() {
        eventLoop = new Thread(this::eventLoop);
        eventLoop.start();
        ChunkController.instance.addOnChunkLoadedHandler(this::onChunkLoaded);
        ChunkController.instance.addOnBlockChangedHandler(this::onBlockChanged);
    }

    public void render(Matrix4f matrix, float xc, float zc, float z, float scale, float multiplier) {
        if (mc.level == null || !isEnabled()) {
            return;
        }

        Dimension dimension = Dimension.get(mc.level);
        Map<ChunkPos, ChunkEntry> chunks = dimensions.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
        for (var entry: chunks.entrySet()) {
            ChunkPos pos = entry.getKey();
            ChunkEntry chunk = entry.getValue();
            float x = (pos.x * 16 - xc) * multiplier;
            float y = (pos.z * 16 - zc) * multiplier;
            int existing = chunk.getExistingFlows();
            int newf = chunk.getNewFlows();

            Color color;
            float alpha = 1;
            if (existing == 0 && newf == 0) {
                color = Color.WHITE;
                alpha = 0;
            } else {
                if (existing == 0) {
                    color = New;
                } else {
                    if (newf == 0) {
                        color = Old;
                    } else {
                        color = SemiNew;
                    }
                    // existing > 0, newf - any
                    /*float perc = newf / existing;
                    if (perc > 1) {
                        perc = 1;
                    }
                    alpha *= (1 - perc * 0.5);*/
                }
            }

            drawSquare(
                    matrix,
                    x, y, scale, z, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
        }
    }

    /*public Stream<BlockPos> getNewBlocks() {
        Dimension dimension = Dimension.get(mc.level);
        var map = dimensions.get(dimension);
        Stream<BlockPos> stream = Stream.empty();
        if (map != null) {
            for (var entry : map.entrySet()) {
                stream = Stream.concat(stream, entry.getValue().getNewBlocks(entry.getKey()));
            }
        }
        return stream;
    }

    public Stream<BlockPos> getOldBlocks() {
        Dimension dimension = Dimension.get(mc.level);
        var map = dimensions.get(dimension);
        Stream<BlockPos> stream = Stream.empty();
        if (map != null) {
            for (var entry : map.entrySet()) {
                stream = Stream.concat(stream, entry.getValue().getOldBlocks(entry.getKey()));
            }
        }
        return stream;
    }*/

    private void drawSquare(Matrix4f matrix, float x, float y, float width, float z, float r, float g, float b, float a) {
        if (a == 0) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        {
            float step = width / 16;
            float x1 = x + step * 3;
            float x2 = x + step * 13;
            float y1 = y + step * 3;
            float y2 = y + step * 13;
            bufferBuilder.vertex(matrix, x1, y1, z).color(1, 1, 1, 1f).endVertex();
            bufferBuilder.vertex(matrix, x1, y2, z).color(1, 1, 1, 1f).endVertex();
            bufferBuilder.vertex(matrix, x2, y2, z).color(1, 1, 1, 1f).endVertex();
            bufferBuilder.vertex(matrix, x2, y1, z).color(1, 1, 1, 1f).endVertex();
        }
        {
            float step = width / 4;
            float x1 = x + step;
            float x2 = x + step * 3;
            float y1 = y + step;
            float y2 = y + step * 3;
            bufferBuilder.vertex(matrix, x1, y1, z + 0.5f).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(matrix, x1, y2, z + 0.5f).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(matrix, x2, y2, z + 0.5f).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(matrix, x2, y1, z + 0.5f).color(r, g, b, a).endVertex();
        }
        /*bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        float step = width / 16;
        float x1 = x + step;
        float x2 = x + step * 15;
        float y1 = y + step;
        float y2 = y + step * 15;
        bufferBuilder.vertex(matrix, x1, y1, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x1, y2, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x1, y2, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x2, y1, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x2, y1, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x1, y1, z).color(r, g, b, a).endVertex();*/
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void eventLoop() {
        try {
            while (true) {
                synchronized (loopWaitEvent) {
                    loopWaitEvent.wait();
                }
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
    }

    private void onChunkLoaded(Dimension dimension, LevelChunk chunk) {
        if (!isEnabled()) {
            return;
        }

        queue.add(() -> {
            Map<ChunkPos, ChunkEntry> chunks = dimensions.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
            ChunkPos pos = chunk.getPos();
            ChunkEntry entry = chunks.computeIfAbsent(pos, p -> new ChunkEntry());
            processChunk(dimension, chunk, entry);
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void onBlockChanged(Dimension dimension, BlockPos pos, BlockState blockState) {
        if (!isEnabled()) {
            return;
        }

        queue.add(() -> {
            if (!(blockState.getBlock() instanceof LiquidBlock)) {
                return;
            }
            FluidState fluidState = blockState.getFluidState();
            if (fluidState.isSource()) {
                return;
            }
            Map<ChunkPos, ChunkEntry> chunks = dimensions.computeIfAbsent(dimension, d -> new ConcurrentHashMap<>());
            ChunkPos chunkPos = new ChunkPos(pos);
            ChunkEntry entry = chunks.computeIfAbsent(chunkPos, p -> new ChunkEntry());
            entry.addNewFlow(pos.getX() & 15, pos.getY() - dimension.getMinY(), pos.getZ() & 15);
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void processChunk(Dimension dimension, LevelChunk chunk, ChunkEntry entry) {
        try {
            while (chunk.getStatus() != ChunkStatus.FULL) {
                Thread.sleep(25);
            }
        } catch (InterruptedException e) {
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            pos.setX(x);
            for (int z = 0; z < 16; z++) {
                pos.setZ(z);
                int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                for (int y = dimension.getMinY(); y <= height; y++) {
                    pos.setY(y);
                    BlockState blockState = chunk.getBlockState(pos);
                    if (blockState.getBlock() instanceof LiquidBlock) {
                        FluidState fluidState = blockState.getFluidState();
                        if (!fluidState.isSource()) {
                            entry.addExistingFlow(x, y - dimension.getMinY(), z);
                        }
                    }
                }
            }
        }
    }

    private boolean isEnabled() {
        return ConfigStore.instance.getConfig().newChunksConfig.enabled;
    }

    private static class ChunkEntry {
        private Set<Integer> existingFlows = new HashSet<>();
        private Set<Integer> newFlows = new HashSet<>();

        public void addExistingFlow(int x, int y, int z) {
            int value = combine(x, y, z);
            if (!newFlows.contains(value)) {
                existingFlows.add(value);
            }
        }

        public void addNewFlow(int x, int y, int z) {
            int value = combine(x, y, z);
            if (!existingFlows.contains(value)) {
                newFlows.add(value);
            }
        }

        public int getExistingFlows() {
            return existingFlows.size();
        }

        public int getNewFlows() {
            return newFlows.size();
        }

        private int combine(int x, int y, int z) {
            return (x | (z << 4)) | (y << 8);
        }

        // TODO: temp
        /*public Stream<BlockPos> getNewBlocks(ChunkPos chunk) {
            return newFlows.stream().map(i -> new BlockPos(chunk.x * 16 + (i & 15), (i >> 8), chunk.z * 16 + ((i >> 4) & 15))).toList().stream();
        }*/

        // TODO: temp
        /*public Stream<BlockPos> getOldBlocks(ChunkPos chunk) {
            return existingFlows.stream().map(i -> new BlockPos(chunk.x * 16 + (i & 15), (i >> 8), chunk.z * 16 + ((i >> 4) & 15))).toList().stream();
        }*/
    }
}