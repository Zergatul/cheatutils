package com.zergatul.cheatutils.chunkoverlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.utils.GuiUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExplorationMiniMapChunkOverlay extends AbstractChunkOverlay {

    private static final Identifier PlayerPosTexture = new Identifier(ModMain.MODID, "textures/mini-map-player.png");
    private static final Identifier CenterPosTexture = new Identifier(ModMain.MODID, "textures/mini-map-center.png");
    private static final Identifier MarkerTexture = new Identifier(ModMain.MODID, "textures/mini-map-marker.png");

    private final Map<Dimension, List<Vector3d>> markers = new HashMap<>();

    public ExplorationMiniMapChunkOverlay(int segmentSize, long updateDelay) {
        super(segmentSize, updateDelay);
    }

    public void addMarker() {
        if (mc.player != null && mc.world != null) {
            Dimension dimension = Dimension.get(mc.world);
            markers.computeIfAbsent(dimension, d -> new ArrayList<>()).add(new Vector3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
    }

    public void clearMarkers() {
        if (mc.world != null) {
            Dimension dimension = Dimension.get(mc.world);
            markers.computeIfAbsent(dimension, d -> new ArrayList<>()).clear();
        }
    }

    @Override
    public int getTranslateZ() {
        return 100;
    }

    @Override
    public boolean isEnabled() {
        return ConfigStore.instance.getConfig().explorationMiniMapConfig.enabled;
    }

    @Override
    public void onPostDrawSegments(Dimension dimension, MatrixStack poseStack, float xp, float zp, float xc, float zc, float multiplier) {
        final int ImageSize = 8;

        for (Vector3d vec: markers.computeIfAbsent(dimension, d -> new ArrayList<>())) {
            RenderSystem.setShaderTexture(0, MarkerTexture);
            GuiUtils.drawTexture(poseStack.peek().getPositionMatrix(),
                    -ImageSize / 2f + ((float) vec.x - xc) * multiplier, -ImageSize / 2f + ((float) vec.z - zc) * multiplier,
                    ImageSize, ImageSize, getTranslateZ() + 2, 0, 0, ImageSize, ImageSize, ImageSize, ImageSize);
        }

        double distanceToPlayer = Math.sqrt((xp - xc) * (xp - xc) + (zp - zc) * (zp - zc));
        if (distanceToPlayer * multiplier > 2 * ImageSize) {
            RenderSystem.setShaderTexture(0, CenterPosTexture);
            GuiUtils.drawTexture(poseStack.peek().getPositionMatrix(),
                    -ImageSize / 2f, -ImageSize / 2f,
                    ImageSize, ImageSize, getTranslateZ() + 3, 0, 0, ImageSize, ImageSize, ImageSize, ImageSize);
        }

        RenderSystem.setShaderTexture(0, PlayerPosTexture);
        GuiUtils.drawTexture(poseStack.peek().getPositionMatrix(),
                -ImageSize / 2f + (xp - xc) * multiplier, -ImageSize / 2f + (zp - zc) * multiplier,
                ImageSize, ImageSize, getTranslateZ() + 4, 0, 0, ImageSize, ImageSize, ImageSize, ImageSize);
    }

    @Override
    protected boolean drawChunk(Dimension dimension, Map<SegmentPos, Segment> segments, WorldChunk chunk) {
        if (chunk.getStatus() != ChunkStatus.FULL) {
            return false;
        }

        ChunkPos chunkPos = chunk.getPos();
        SegmentPos segmentPos = new SegmentPos(chunkPos, segmentSize);

        addToRenderQueue(new RenderThreadQueueItem(() -> {
            if (!segments.containsKey(segmentPos)) {
                segments.put(segmentPos, new Segment(segmentPos, segmentSize));
            }
        }, () -> {
            Segment segment = segments.get(segmentPos);
            int xf = Math.floorMod(chunkPos.x, segmentSize) * 16;
            int yf = Math.floorMod(chunkPos.z, segmentSize) * 16;

            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    drawPixel(dimension, xf, yf, dx, dz, segment, chunk);
                }
            }

            addToRenderQueue(new RenderThreadQueueItem(segment::onChange));
        }));

        return true;
    }

    @Override
    protected void processBlockChange(Dimension dimension, ChunkPos chunkPos, Segment segment, BlockPos pos, BlockState state) {
        if (mc.world == null || segment == null) {
            return;
        }

        if (dimension.isNether()) {
            int xf = Math.floorMod(chunkPos.x, segmentSize) * 16;
            int yf = Math.floorMod(chunkPos.z, segmentSize) * 16;
            boolean updated = drawPixel(dimension, xf, yf, Math.floorMod(pos.getX(), 16), Math.floorMod(pos.getZ(), 16), segment, mc.world.getChunk(chunkPos.x, chunkPos.z));
            if (updated && !segment.updated) {
                segment.updated = true;
                segment.updateTime = System.nanoTime();
                addUpdatedSegment(segment);
            }
        } else {
            WorldChunk chunk = mc.world.getChunk(chunkPos.x, chunkPos.z);
            int dx = Math.floorMod(pos.getX(), 16);
            int dz = Math.floorMod(pos.getZ(), 16);
            int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(dx, dz);
            if (pos.getY() >= height) {
                int xf = Math.floorMod(chunkPos.x, segmentSize) * 16;
                int yf = Math.floorMod(chunkPos.z, segmentSize) * 16;
                boolean updated = drawPixel(dimension, xf, yf, dx, dz, segment, chunk);
                if (updated && !segment.updated) {
                    segment.updated = true;
                    segment.updateTime = System.nanoTime();
                    addUpdatedSegment(segment);
                }
            }
        }
    }

    private boolean drawPixel(Dimension dimension, int xf, int yf, int dx, int dz, Segment segment, WorldChunk chunk) {
        if (dimension.hasCeiling()) {
            for (int y1 = dimension.getMinY() + dimension.getLogicalHeight() - 1; y1 >= dimension.getMinY(); y1--) {
                BlockPos pos = new BlockPos(dx, y1, dz);
                BlockState state = chunk.getBlockState(pos);
                if (state.isAir()) {
                    // first non-air block below
                    for (int y2 = y1 - 1; y2 >= dimension.getMinY(); y2--) {
                        pos = new BlockPos(dx, y2, dz);
                        state = chunk.getBlockState(pos);
                        if (!state.isAir()) {
                            MapColor materialColor = state.getMapColor(mc.world, pos);
                            if (materialColor == MapColor.CLEAR) {
                                continue;
                            }
                            int color = convert(materialColor.color);
                            if (segment.image.getColor(xf + dx, yf + dz) != color) {
                                segment.image.setColor(xf + dx, yf + dz, color);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                    break;
                }
            }
        }

        int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(dx, dz);
        for (int y = height; y >= dimension.getMinY(); y--) {
            BlockPos pos = new BlockPos(dx, y, dz);
            BlockState state = chunk.getBlockState(pos);
            MapColor materialColor = state.getMapColor(mc.world, pos);
            if (materialColor != MapColor.CLEAR) {
                int color = convert(materialColor.color);
                if (segment.image.getColor(xf + dx, yf + dz) != color) {
                    segment.image.setColor(xf + dx, yf + dz, color);
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    private static int convert(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color) & 0xFF;
        return 0xFF000000 | (blue << 16) | (green << 8) | red;
    }
}