package com.zergatul.cheatutils.controllers;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ExplorationMiniMapConfig;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExplorationMiniMapController {

    public static final ExplorationMiniMapController instance = new ExplorationMiniMapController();

    private static final int SegmentSize = 16;
    private static final int TranslateZ = 250;
    private static final float MinScale = 1 * SegmentSize;
    private static final float MaxScale = 32 * SegmentSize;
    private static final float ScaleStep = 1.3f;
    private static final ResourceLocation PlayerPosTexture = new ResourceLocation(ModMain.MODID, "textures/mini-map-player.png");
    private static final ResourceLocation CenterPosTexture = new ResourceLocation(ModMain.MODID, "textures/mini-map-center.png");
    private static final ResourceLocation MarkerTexture = new ResourceLocation(ModMain.MODID, "textures/mini-map-marker.png");

    private final Minecraft mc = Minecraft.getInstance();
    private float scale = 16 * SegmentSize;
    private final Object loopWaitEvent = new Object();
    private final Thread eventLoop;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final Queue<RenderThreadQueueItem> renderQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Runnable> endTickQueue = new ConcurrentLinkedQueue<>();
    private final Map<Dimension, Map<SegmentPos, Segment>> dimensions = new ConcurrentHashMap<>();
    private final Set<Segment> updatedSegments = new HashSet<>();
    private final List<Segment> textureUploaded = new ArrayList<>();
    private final Map<Dimension, List<Vector3d>> markers = new HashMap<>();

    private ExplorationMiniMapController() {
        eventLoop = new Thread(this::eventLoopThreadFunc);
        eventLoop.start();

        ChunkController.instance.addOnChunkLoadedHandler(this::onChunkLoaded);
        ChunkController.instance.addOnBlockChangedHandler(this::onBlockChanged);
    }

    public void onChanged() {
        if (ConfigStore.instance.getConfig().explorationMiniMapConfig.enabled) {
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

    @SubscribeEvent
    public void render(RenderGuiEvent.Post event) {
        ExplorationMiniMapConfig config = ConfigStore.instance.getConfig().explorationMiniMapConfig;
        if (!config.enabled) {
            return;
        }

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
        long delay = config.dynamicUpdateDelay * 1000000L;
        for (Segment segment : updatedSegments) {
            if (now - segment.updateTime > delay) {
                segment.onChange();
                segment.updated = false;
                segment.updateTime = 0;
                updatedSegments.add(segment);
            }
        }

        for (Segment segment : textureUploaded) {
            updatedSegments.remove(segment);
        }

        if (!mc.options.keyPlayerList.isDown()) {
            return;
        }

        if (Screen.hasAltDown()) {
            return;
        }

        if (mc.player == null || mc.level == null) {
            return;
        }

        float frameTime = event.getPartialTick();
        float xp = (float) Mth.lerp(frameTime, mc.player.xo, mc.player.getX());
        float zp = (float) Mth.lerp(frameTime, mc.player.zo, mc.player.getZ());
        float xc = (float) mc.gameRenderer.getMainCamera().getPosition().x;
        float zc = (float) mc.gameRenderer.getMainCamera().getPosition().z;
        float yRot = mc.gameRenderer.getMainCamera().getYRot();

        event.getPoseStack().pushPose();
        event.getPoseStack().setIdentity();
        event.getPoseStack().translate(mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2, TranslateZ);
        event.getPoseStack().mulPose(Vector3f.ZN.rotationDegrees(yRot));
        event.getPoseStack().mulPose(Vector3f.XN.rotationDegrees(180));
        event.getPoseStack().mulPose(Vector3f.YN.rotationDegrees(180));
        RenderSystem.applyModelViewMatrix();

        //RenderSystem.enableDepthTest();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        //RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 0.5f);

        float multiplier = 1f / (16 * SegmentSize) * scale;
        Dimension dimension = Dimension.get(mc.level);
        Map<SegmentPos, Segment> segments = dimensions.computeIfAbsent(dimension, d -> new HashMap<>());
        for (Segment segment : segments.values()) {
            if (segment.texture == null) {
                continue;
            }

            RenderSystem.setShaderTexture(0, segment.texture.getId());

            float x = (segment.pos.x * 16 * SegmentSize - xc) * multiplier;
            float y = (segment.pos.z * 16 * SegmentSize - zc) * multiplier;

            drawTexture(
                    event.getPoseStack().last().pose(),
                    x, y, scale, scale, 100,
                    0, 0, 16 * SegmentSize, 16 * SegmentSize,
                    16 * SegmentSize, 16 * SegmentSize);
        }

        NewChunksController.instance.render(event.getPoseStack().last().pose(), xc, zc, 101, scale / SegmentSize, multiplier);

        final int ImageSize = 8;

        for (Vector3d vec : markers.computeIfAbsent(dimension, d -> new ArrayList<>())) {
            RenderSystem.setShaderTexture(0, MarkerTexture);
            drawTexture(event.getPoseStack().last().pose(),
                    -ImageSize / 2 + ((float) vec.x - xc) * multiplier, -ImageSize / 2 + ((float) vec.z - zc) * multiplier,
                    ImageSize, ImageSize, 102, 0, 0, ImageSize, ImageSize, ImageSize, ImageSize);
        }

        double distanceToPlayer = Math.sqrt((xp - xc) * (xp - xc) + (zp - zc) * (zp - zc));
        if (distanceToPlayer * multiplier > 2 * ImageSize) {
            RenderSystem.setShaderTexture(0, CenterPosTexture);
            drawTexture(event.getPoseStack().last().pose(),
                    -ImageSize / 2, -ImageSize / 2,
                    ImageSize, ImageSize, 103, 0, 0, ImageSize, ImageSize, ImageSize, ImageSize);
        }

        RenderSystem.setShaderTexture(0, PlayerPosTexture);
        drawTexture(event.getPoseStack().last().pose(),
                -ImageSize / 2 + (xp - xc) * multiplier, -ImageSize / 2 + (zp - zc) * multiplier,
                ImageSize, ImageSize, 104, 0, 0, ImageSize, ImageSize, ImageSize, ImageSize);

        event.getPoseStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @SubscribeEvent
    public void onPreRenderGameOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == GuiOverlayManager.findOverlay(VanillaGuiOverlay.PLAYER_LIST.id())) {
            if (!ConfigStore.instance.getConfig().explorationMiniMapConfig.enabled) {
                return;
            }
            if (Screen.hasAltDown()) {
                return;
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!ConfigStore.instance.getConfig().explorationMiniMapConfig.enabled) {
            return;
        }

        if (!mc.options.keyPlayerList.isDown()) {
            return;
        }

        event.setCanceled(true);

        if (event.getScrollDelta() >= 1.0d) {
            if (scale < MaxScale) {
                scale *= ScaleStep;
            }
        }

        if (event.getScrollDelta() <= -1.0d) {
            if (scale > MinScale) {
                scale /= ScaleStep;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (endTickQueue.size() > 0) {
                endTickQueue.remove().run();
            }
        }
    }

    public void addMarker() {
        if (mc.player != null && mc.level != null) {
            Dimension dimension = Dimension.get(mc.level);
            markers.computeIfAbsent(dimension, d -> new ArrayList<>()).add(new Vector3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
    }

    public void clearMarkers() {
        if (mc.level != null) {
            Dimension dimension = Dimension.get(mc.level);
            markers.computeIfAbsent(dimension, d -> new ArrayList<>()).clear();
        }
    }

    private void onChunkLoaded(Dimension dimension, LevelChunk chunk) {
        if (!ConfigStore.instance.getConfig().explorationMiniMapConfig.enabled) {
            return;
        }

        Map<SegmentPos, Segment> segments = dimensions.computeIfAbsent(dimension, d -> new HashMap<>());

        queue.add(() -> drawChunk(dimension, segments, chunk));

        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void onBlockChanged(Dimension dimension, BlockPos pos, BlockState state) {
        ExplorationMiniMapConfig config = ConfigStore.instance.getConfig().explorationMiniMapConfig;
        if (!config.enabled || !config.dynamicUpdate) {
            return;
        }

        endTickQueue.add(() -> {
            if (mc.level == null) {
                return;
            }
            var chunkPos = new ChunkPos(pos);
            var segmentPos = new SegmentPos(chunkPos);
            Map<SegmentPos, Segment> segments = dimensions.computeIfAbsent(dimension, d -> new HashMap<>());
            Segment segment = segments.get(segmentPos);
            if (segment != null) {
                if (dimension.isNether()) {
                    int xf = Math.floorMod(chunkPos.x, SegmentSize) * 16;
                    int yf = Math.floorMod(chunkPos.z, SegmentSize) * 16;
                    drawPixel(dimension, xf, yf, Math.floorMod(pos.getX(), 16), Math.floorMod(pos.getZ(), 16), segment, mc.level.getChunk(chunkPos.x, chunkPos.z));
                    if (!segment.updated) {
                        segment.updated = true;
                        segment.updateTime = System.nanoTime();
                        updatedSegments.add(segment);
                    }
                } else {
                    LevelChunk chunk = mc.level.getChunk(chunkPos.x, chunkPos.z);
                    int dx = Math.floorMod(pos.getX(), 16);
                    int dz = Math.floorMod(pos.getZ(), 16);
                    int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, dx, dz);
                    if (pos.getY() >= height) {
                        int xf = Math.floorMod(chunkPos.x, SegmentSize) * 16;
                        int yf = Math.floorMod(chunkPos.z, SegmentSize) * 16;
                        drawPixel(dimension, xf, yf, dx, dz, segment, chunk);
                        if (!segment.updated) {
                            segment.updated = true;
                            segment.updateTime = System.nanoTime();
                            updatedSegments.add(segment);
                        }
                    }
                }
            }
        });
    }

    private void drawTexture(Matrix4f matrix, float x, float y, float width, float height, float z, int texX, int texY, int texWidth, int texHeight, int texSizeX, int texSizeY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y, z).uv(1F * texX / texSizeX, 1F * texY / texSizeY).endVertex();
        bufferBuilder.vertex(matrix, x, y + height, z).uv(1F * texX / texSizeX, 1F * (texY + texHeight) / texSizeY).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, z).uv(1F * (texX + texWidth) / texSizeX, 1F * (texY + texHeight) / texSizeY).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, z).uv(1F * (texX + texWidth) / texSizeX, 1F * texY / texSizeY).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void drawChunk(Dimension dimension, Map<SegmentPos, Segment> segments, LevelChunk chunk) {
        while (chunk.getStatus() != ChunkStatus.FULL) {
            try {
                Thread.sleep(30);
            }
            catch (InterruptedException e) {
                return;
            }
        }

        ChunkPos chunkPos = chunk.getPos();
        SegmentPos segmentPos = new SegmentPos(chunkPos);

        renderQueue.add(new RenderThreadQueueItem(() -> {
            if (!segments.containsKey(segmentPos)) {
                segments.put(segmentPos, new Segment(segmentPos));
            }
        }, () -> {
            Segment segment = segments.get(segmentPos);
            drawChunk(dimension, segment, chunkPos, chunk);
        }));
    }

    private void drawChunk(Dimension dimension, Segment segment, ChunkPos chunkPos, LevelChunk chunk) {
        int xf = Math.floorMod(chunkPos.x, SegmentSize) * 16;
        int yf = Math.floorMod(chunkPos.z, SegmentSize) * 16;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                drawPixel(dimension, xf, yf, dx, dz, segment, chunk);
            }
        }

        renderQueue.add(new RenderThreadQueueItem(segment::onChange));
    }

    private void drawPixel(Dimension dimension, int xf, int yf, int dx, int dz, Segment segment, LevelChunk chunk) {
        boolean pixelSet = false;
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
                            MaterialColor materialColor = state.getMapColor(mc.level, pos);
                            if (materialColor == MaterialColor.NONE) {
                                continue;
                            }
                            segment.image.setPixelRGBA(xf + dx, yf + dz, convert(materialColor.col));
                            pixelSet = true;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (!pixelSet) {
            int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, dx, dz);
            for (int y = height; y >= dimension.getMinY(); y--) {
                BlockPos pos = new BlockPos(dx, y, dz);
                BlockState state = chunk.getBlockState(pos);
                MaterialColor materialColor = state.getMapColor(mc.level, pos);
                if (materialColor != MaterialColor.NONE) {
                    segment.image.setPixelRGBA(xf + dx, yf + dz, convert(materialColor.col));
                    break;
                }
            }
        }
    }

    private void updateFromMapItem(Level level, Entity entity, MapItemSavedData map) {
        if (level.dimension() == map.dimension && entity instanceof Player) {
            int blocksPerPixel = 1 << map.scale;
            int xCenter = map.x;
            int zCenter = map.z;
            int mx = Mth.floor(entity.getX() - (double)xCenter) / blocksPerPixel + 64;
            int mz = Mth.floor(entity.getZ() - (double)zCenter) / blocksPerPixel + 64;
            int width = 128 / blocksPerPixel;
            if (level.dimensionType().hasCeiling()) {
                width /= 2;
            }

            MapItemSavedData.HoldingPlayer mapitemsaveddata$holdingplayer = map.getHoldingPlayer((Player)entity);
            ++mapitemsaveddata$holdingplayer.step;
            boolean flag = false;

            for(int x = mx - width + 1; x < mx + width; ++x) {
                if ((x & 15) == (mapitemsaveddata$holdingplayer.step & 15) || flag) {
                    flag = false;
                    double d0 = 0.0D;

                    for(int y = mz - width - 1; y < mz + width; ++y) {
                        if (x >= 0 && y >= -1 && x < 128 && y < 128) {
                            int dx = x - mx;
                            int dy = y - mz;
                            boolean flag1 = dx * dx + dy * dy > (width - 2) * (width - 2);
                            int x2 = (xCenter / blocksPerPixel + x - 64) * blocksPerPixel;
                            int z2 = (zCenter / blocksPerPixel + y - 64) * blocksPerPixel;
                            Multiset<MaterialColor> multiset = LinkedHashMultiset.create();
                            LevelChunk levelchunk = level.getChunkAt(new BlockPos(x2, 0, z2));
                            if (!levelchunk.isEmpty()) {
                                ChunkPos chunkpos = levelchunk.getPos();
                                int x3 = x2 & 15;
                                int z3 = z2 & 15;
                                int k3 = 0;
                                double d1 = 0.0D;
                                if (level.dimensionType().hasCeiling()) {
                                    int l3 = x2 + z2 * 231871;
                                    l3 = l3 * l3 * 31287121 + l3 * 11;
                                    if ((l3 >> 20 & 1) == 0) {
                                        multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                                    } else {
                                        multiset.add(Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                                    }

                                    d1 = 100.0D;
                                } else {
                                    BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();
                                    BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                                    for(int dx2 = 0; dx2 < blocksPerPixel; ++dx2) {
                                        for(int dz2 = 0; dz2 < blocksPerPixel; ++dz2) {
                                            int height = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, dx2 + x3, dz2 + z3) + 1;
                                            BlockState blockstate;
                                            if (height <= level.getMinBuildHeight() + 1) {
                                                blockstate = Blocks.BEDROCK.defaultBlockState();
                                            } else {
                                                do {
                                                    --height;
                                                    blockpos$mutableblockpos1.set(chunkpos.getMinBlockX() + dx2 + x3, height, chunkpos.getMinBlockZ() + dz2 + z3);
                                                    blockstate = levelchunk.getBlockState(blockpos$mutableblockpos1);
                                                } while(blockstate.getMapColor(level, blockpos$mutableblockpos1) == MaterialColor.NONE && height > level.getMinBuildHeight());

                                                if (height > level.getMinBuildHeight() && !blockstate.getFluidState().isEmpty()) {
                                                    int l4 = height - 1;
                                                    blockpos$mutableblockpos.set(blockpos$mutableblockpos1);

                                                    BlockState blockstate1;
                                                    do {
                                                        blockpos$mutableblockpos.setY(l4--);
                                                        blockstate1 = levelchunk.getBlockState(blockpos$mutableblockpos);
                                                        ++k3;
                                                    } while(l4 > level.getMinBuildHeight() && !blockstate1.getFluidState().isEmpty());

                                                    blockstate = this.getCorrectStateForFluidBlock(level, blockstate, blockpos$mutableblockpos1);
                                                }
                                            }

                                            map.checkBanners(level, chunkpos.getMinBlockX() + dx2 + x3, chunkpos.getMinBlockZ() + dz2 + z3);
                                            d1 += (double)height / (double)(blocksPerPixel * blocksPerPixel);
                                            multiset.add(blockstate.getMapColor(level, blockpos$mutableblockpos1));
                                        }
                                    }
                                }

                                k3 /= blocksPerPixel * blocksPerPixel;
                                MaterialColor materialcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.NONE);
                                MaterialColor.Brightness materialcolor$brightness;
                                if (materialcolor == MaterialColor.WATER) {
                                    double d2 = (double)k3 * 0.1D + (double)(x + y & 1) * 0.2D;
                                    if (d2 < 0.5D) {
                                        materialcolor$brightness = MaterialColor.Brightness.HIGH;
                                    } else if (d2 > 0.9D) {
                                        materialcolor$brightness = MaterialColor.Brightness.LOW;
                                    } else {
                                        materialcolor$brightness = MaterialColor.Brightness.NORMAL;
                                    }
                                } else {
                                    double d3 = (d1 - d0) * 4.0D / (double)(blocksPerPixel + 4) + ((double)(x + y & 1) - 0.5D) * 0.4D;
                                    if (d3 > 0.6D) {
                                        materialcolor$brightness = MaterialColor.Brightness.HIGH;
                                    } else if (d3 < -0.6D) {
                                        materialcolor$brightness = MaterialColor.Brightness.LOW;
                                    } else {
                                        materialcolor$brightness = MaterialColor.Brightness.NORMAL;
                                    }
                                }

                                d0 = d1;
                                if (y >= 0 && dx * dx + dy * dy < width * width && (!flag1 || (x + y & 1) != 0)) {
                                    flag |= map.updateColor(x, y, materialcolor.getPackedId(materialcolor$brightness));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private BlockState getCorrectStateForFluidBlock(Level p_42901_, BlockState p_42902_, BlockPos p_42903_) {
        FluidState fluidstate = p_42902_.getFluidState();
        return !fluidstate.isEmpty() && !p_42902_.isFaceSturdy(p_42901_, p_42903_, Direction.UP) ? fluidstate.createLegacyBlock() : p_42902_;
    }

    private static int convert(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color) & 0xFF;
        return 0xFF000000 | (blue << 16) | (green << 8) | red;
    }

    private static class Segment {
        public final SegmentPos pos;
        public final NativeImage image;
        public final DynamicTexture texture;
        public boolean updated;
        public long updateTime;

        // do we need lastUpdate logic to improve performance?

        public Segment(SegmentPos pos) {
            this.pos = pos;
            this.image = new NativeImage(SegmentSize * 16, SegmentSize * 16, true);
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

    private static class SegmentPos {
        public int x;
        public int z;

        public SegmentPos(ChunkPos pos) {
            this.x = Math.floorDiv(pos.x, SegmentSize);
            this.z = Math.floorDiv(pos.z, SegmentSize);
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

    private static class RenderThreadQueueItem {
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