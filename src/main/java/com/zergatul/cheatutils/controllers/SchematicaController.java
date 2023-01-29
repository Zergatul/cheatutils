package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.collections.IntArrayList;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SchematicaConfig;
import com.zergatul.cheatutils.schematics.PlacingConverter;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchemaFile;
import com.zergatul.cheatutils.utils.*;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.BlockUpdateEvent;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SchematicaController {

    public static final SchematicaController instance = new SchematicaController();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Entry> entries = new ArrayList<>();
    private final RandomSource random = new JavaRandom(0);
    private final long[] lastSlotUsage = new long[9];

    private SchematicaController() {
        ModApiWrapper.ScannerChunkLoaded.add(this::onChunkLoaded);
        ModApiWrapper.ScannerBlockUpdated.add(this::onBlockUpdated);
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
        ModApiWrapper.RenderSolidBlocksEnd.add(this::onRenderSolidBlocksEnd);
        ModApiWrapper.RenderWorldLast.add(this::onRender);

        Arrays.fill(lastSlotUsage, Long.MIN_VALUE);
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void place(SchemaFile file, PlacingSettings placing) {
        if (mc.level != null) {
            Entry entry = new Entry(mc.level, file, placing);
            entries.add(entry);
            ChunkController.instance.getLoadedChunks().forEach(p -> entry.onChunkLoaded(p.getSecond()));
        }
    }

    private synchronized void onClientTickEnd() {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled || !config.autoBuild) {
            return;
        }

        if (mc.level == null || mc.player == null) {
            return;
        }

        Vec3 eyes = mc.player.getEyePosition();
        int xp = (int)Math.round(eyes.x);
        int yp = (int)Math.round(eyes.y);
        int zp = (int)Math.round(eyes.z);
        int distance = (int)Math.round(config.autoBuildDistance) + 1;
        double maxDistanceSqr = config.autoBuildDistance * config.autoBuildDistance;
        ItemStack itemInHand = mc.player.getMainHandItem();

        Block blockInHand;
        if (itemInHand.getItem() instanceof BlockItem blockItem) {
            blockInHand = blockItem.getBlock();
        } else {
            if (config.autoSelectItems) {
                blockInHand = null;
            } else {
                return;
            }
        }

        BlockUtils.PlaceBlockPlan bestPlan = null;
        double bestDistance = Double.MAX_VALUE;
        BlockState finalState = null;
        Dimension dimension = Dimension.get(mc.level);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -distance; dx <= distance; dx++) {
            pos.setX(xp + dx);
            for (int dy = -distance; dy <= distance; dy++) {
                pos.setY(yp + dy);
                if (pos.getY() < -64 || pos.getY() >= 320) {
                    continue;
                }
                for (int dz = -distance; dz <= distance; dz++) {
                    pos.setZ(zp + dz);

                    double blockDx = pos.getX() + 0.5 - xp;
                    double blockDy = pos.getY() + 0.5 - yp;
                    double blockDz = pos.getZ() + 0.5 - zp;
                    double d2 = blockDx * blockDx + blockDy * blockDy + blockDz * blockDz;
                    if (d2 > bestDistance || d2 > maxDistanceSqr) {
                        continue;
                    }

                    for (Entry entry : entries) {
                        if (entry.dimension != dimension) {
                            continue;
                        }

                        BlockState state = entry.getBlockState(pos.getX(), pos.getY(), pos.getZ());
                        if (state.isAir()) {
                            continue;
                        }

                        BlockUtils.PlaceBlockPlan plan = BlockUtils.getPlacingPlan(pos);
                        if (plan != null) {
                            bestPlan = plan;
                            bestDistance = d2;
                            finalState = state;
                        }
                    }
                }
            }
        }

        if (bestPlan != null) {
            if (config.autoSelectItems) {
                if (selectItem(config, finalState.getBlock()))  {
                    blockInHand = finalState.getBlock();
                }
            }
            if (blockInHand == finalState.getBlock() && mc.level.getBlockState(pos).getMaterial().isReplaceable()) {
                BlockUtils.applyPlacingPlan(bestPlan, config.useShift);
            }
        }
    }

    private synchronized void onRenderSolidBlocksEnd(RenderWorldLastEvent event) {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        Vec3 view = event.getCamera().getPosition();
        Dimension dimension = Dimension.get(mc.level);

        if (config.showMissingBlockGhosts) {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend(); // transparency
            RenderSystem.enableTexture();
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 1f/*0.6f*/);

            Map<BlockPos, BlockState> ghosts = new HashMap<>();
            for (Entry entry : entries) {
                if (entry.dimension != dimension) {
                    continue;
                }
                entry.forEachMissingState(view, config.missingBlockGhostsMaxDistance, (pos, state) -> {
                    ghosts.put(pos.immutable(), state);
                });
            }

            BlockPos.MutableBlockPos neighPos = new BlockPos.MutableBlockPos();
            List<BakedQuad> quads = new ArrayList<>();
            for (var mapEntry : ghosts.entrySet()) {
                BlockPos pos = mapEntry.getKey();
                BlockState state = mapEntry.getValue();
                BakedModel model = mc.getBlockRenderer().getBlockModel(state);

                quads.clear();
                for (var direction : Direction.values()) {
                    quads.addAll(model.getQuads(state, direction, random, ModelData.EMPTY, null));
                }
                quads.addAll(model.getQuads(state, null, random, ModelData.EMPTY, null));
                /*for (var direction : Direction.values())*/ {
                    /*neighPos.setX(pos.getX() + direction.getStepX());
                    neighPos.setY(pos.getY() + direction.getStepY());
                    neighPos.setZ(pos.getZ() + direction.getStepZ());*/
                    /*if (!ghosts.containsKey(neighPos))*/ {
                        //List<BakedQuad> quads = model.getQuads(state, null/*direction*/, random, ModelData.EMPTY, null);
                        for (BakedQuad quad : quads) {
                            TextureAtlasSprite sprite = quad.getSprite();
                            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                            RenderSystem.setShaderTexture(0, sprite.atlasLocation());

                            int[] vertices = quad.getVertices();
                            for (int i = 0; i < 4; i++) {
                                int offset = i * 8;
                                float x = Float.intBitsToFloat(vertices[offset]);
                                float y = Float.intBitsToFloat(vertices[offset + 1]);
                                float z = Float.intBitsToFloat(vertices[offset + 2]);
                                int color = vertices[offset + 3];
                                float u = Float.intBitsToFloat(vertices[offset + 4]);
                                float v = Float.intBitsToFloat(vertices[offset + 5]);

                                bufferBuilder.vertex(
                                        pos.getX() + x - view.x,
                                        pos.getY() + y - view.y,
                                        pos.getZ() + z - view.z)
                                        .uv(u, v).color(color).endVertex();
                            }

                            SharedVertexBuffer.instance.bind();
                            SharedVertexBuffer.instance.upload(bufferBuilder.end());
                            SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionTexShader());
                            VertexBuffer.unbind();
                        }
                    }
                }
            }
        }
    }

    private synchronized void onRender(RenderWorldLastEvent event) {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        Vec3 view = event.getCamera().getPosition();
        Dimension dimension = Dimension.get(mc.level);

        if (config.showMissingBlockTracers) {
            Vec3 tracerCenter = event.getTracerCenter();
            double tracerX = tracerCenter.x;
            double tracerY = tracerCenter.y;
            double tracerZ = tracerCenter.z;

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

            for (Entry entry : entries) {
                if (entry.dimension != dimension) {
                    continue;
                }
                entry.forEachMissing(view, config.missingBlockTracersMaxDistance, pos -> {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                });
            }

            GlUtils.renderLines(bufferBuilder, event.getMatrixStack().last().pose(), event.getProjectionMatrix());
        }

        if (config.showMissingBlockCubes) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

            for (Entry entry : entries) {
                if (entry.dimension != dimension) {
                    continue;
                }
                entry.forEachMissing(view, config.missingBlockCubesMaxDistance, pos -> {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;
                    GlUtils.drawCube(bufferBuilder, x1, y1, z1, x2, y2, z2);
                });
            }

            GlUtils.renderLines(bufferBuilder, event.getMatrixStack().last().pose(), event.getProjectionMatrix());
        }

        if (config.showWrongBlockTracers) {
            Vec3 tracerCenter = event.getTracerCenter();
            double tracerX = tracerCenter.x;
            double tracerY = tracerCenter.y;
            double tracerZ = tracerCenter.z;

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 0.6f);

            for (Entry entry : entries) {
                if (entry.dimension != dimension) {
                    continue;
                }
                entry.forEachWrong(view, config.wrongBlockTracersMaxDistance, pos -> {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                });
            }

            GlUtils.renderLines(bufferBuilder, event.getMatrixStack().last().pose(), event.getProjectionMatrix());
        }

        if (config.showWrongBlockCubes) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 0.6f);

            for (Entry entry : entries) {
                if (entry.dimension != dimension) {
                    continue;
                }
                entry.forEachWrong(view, config.wrongBlockCubesMaxDistance, pos -> {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;
                    GlUtils.drawCube(bufferBuilder, x1, y1, z1, x2, y2, z2);
                });
            }

            GlUtils.renderLines(bufferBuilder, event.getMatrixStack().last().pose(), event.getProjectionMatrix());
        }
    }

    private synchronized void onChunkLoaded(LevelChunk chunk) {
        for (Entry entry : entries) {
            entry.onChunkLoaded(chunk);
        }
    }

    private synchronized void onBlockUpdated(BlockUpdateEvent event) {
        for (Entry entry : entries) {
            entry.onBlockUpdated(event);
        }
    }

    private static void modelPoint(int[] vertices, int index, BlockPos pos) {

    }

    private boolean selectItem(SchematicaConfig config, Block block) {
        Inventory inventory = mc.player.getInventory();

        // search on hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == block) {
                    lastSlotUsage[i] = System.nanoTime();
                    inventory.selected = i;
                    return true;
                }
            }
        }

        if (config.autoSelectSlots.length == 0) {
            return false;
        }

        // search in inventory
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == block) {
                    long minTime = Long.MAX_VALUE;
                    int minSlot = config.autoSelectSlots[0];
                    for (int slot : config.autoSelectSlots) {
                        if (lastSlotUsage[slot - 1] < minTime) {
                            minTime = lastSlotUsage[slot - 1];
                            minSlot = slot - 1;
                        }
                    }

                    InventoryUtils.moveItemStack(new InventorySlot(i), new InventorySlot(minSlot));
                    lastSlotUsage[minSlot] = System.nanoTime();
                    inventory.selected = minSlot;
                    return true;
                }
            }
        }

        return false;
    }

    private static class Entry {

        public final Dimension dimension;
        public final int x1, x2, y1, y2, z1, z2;
        public final Map<Long, Chunk> chunks;

        public Entry(ClientLevel level, SchemaFile file, PlacingSettings placing) {
            dimension = Dimension.get(level);

            PlacingConverter converter = new PlacingConverter(placing, file.getWidth(), file.getHeight(), file.getLength());

            x1 = placing.x;
            x2 = x1 + converter.getWidth();
            y1 = placing.y;
            y2 = y1 + converter.getHeight();
            z1 = placing.z;
            z2 = z1 + converter.getLength();

            chunks = new HashMap<>();
            for (int x = 0; x < file.getWidth(); x++) {
                for (int y = 0; y < file.getHeight(); y++) {
                    for (int z = 0; z < file.getLength(); z++) {
                        BlockState state = file.getBlockState(x, y, z);
                        if (!state.isAir()) {
                            PlacingConverter.Vec3iMutable vec = converter.convert(x, y, z);
                            int wx = x1 + vec.x;
                            int wy = y1 + vec.y;
                            int wz = z1 + vec.z;
                            long chunkIndex = blockToChunkIndex(wx, wz);
                            Chunk chunk = chunks.get(chunkIndex);
                            if (chunk == null) {
                                chunk = new Chunk(wx & 0xFFFFFFF0, y1, y2, wz & 0xFFFFFFF0);
                                chunks.put(chunkIndex, chunk);
                            }
                            chunk.setBlockState(wx & 0x0F, wy, wz & 0x0F, state);
                        }
                    }
                }
            }
        }

        public void forEachMissing(Vec3 view, double distance, Consumer<BlockPos> consumer) {
            // 16 * sqrt(3) = 27, 16x16x16 diagonal
            // use 25 for perf
            double chunkDistance2 = (distance + 25) * (distance + 25);
            double distance2 = distance * distance;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (Chunk chunk : chunks.values()) {
                if (chunk.getDistanceSqrTo(view) > chunkDistance2) {
                    continue;
                }

                for (ChunkSection section : chunk.sections) {
                    if (section == null) {
                        continue;
                    }
                    if (section.getDistanceSqrTo(view) > chunkDistance2) {
                        continue;
                    }

                    int size = section.missing.size();
                    int[] elements = section.missing.getElements();
                    for (int i = 0; i < size; i++) {
                        int value = elements[i];
                        pos.setX(section.minX | ChunkSection.toX(value));
                        pos.setY(section.minY | ChunkSection.toY(value));
                        pos.setZ(section.minZ | ChunkSection.toZ(value));
                        if (pos.distToCenterSqr(view) < distance2) {
                            consumer.accept(pos);
                        }
                    }
                }
            }
        }

        public void forEachMissingState(Vec3 view, double distance, BiConsumer<BlockPos, BlockState> consumer) {
            // 16 * sqrt(3) = 27, 16x16x16 diagonal
            // use 25 for perf
            double chunkDistance2 = (distance + 25) * (distance + 25);
            double distance2 = distance * distance;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (Chunk chunk : chunks.values()) {
                if (chunk.getDistanceSqrTo(view) > chunkDistance2) {
                    continue;
                }

                for (ChunkSection section : chunk.sections) {
                    if (section == null) {
                        continue;
                    }
                    if (section.getDistanceSqrTo(view) > chunkDistance2) {
                        continue;
                    }

                    int size = section.missing.size();
                    int[] elements = section.missing.getElements();
                    for (int i = 0; i < size; i++) {
                        int value = elements[i];
                        pos.setX(section.minX | ChunkSection.toX(value));
                        pos.setY(section.minY | ChunkSection.toY(value));
                        pos.setZ(section.minZ | ChunkSection.toZ(value));
                        if (pos.distToCenterSqr(view) < distance2) {
                            consumer.accept(pos, section.getBlockState(pos.getX() & 0x0F, pos.getY() & 0x0F, pos.getZ() & 0x0F));
                        }
                    }
                }
            }
        }

        public void forEachWrong(Vec3 view, double distance, Consumer<BlockPos> consumer) {
            // 16 * sqrt(3) = 27, 16x16x16 diagonal
            // use 25 for perf
            double chunkDistance2 = (distance + 25) * (distance + 25);
            double distance2 = distance * distance;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (Chunk chunk : chunks.values()) {
                if (chunk.getDistanceSqrTo(view) > chunkDistance2) {
                    continue;
                }

                for (ChunkSection section : chunk.sections) {
                    if (section == null) {
                        continue;
                    }
                    if (section.getDistanceSqrTo(view) > chunkDistance2) {
                        continue;
                    }

                    int size = section.wrong.size();
                    int[] elements = section.wrong.getElements();
                    for (int i = 0; i < size; i++) {
                        int value = elements[i];
                        pos.setX(section.minX | ChunkSection.toX(value));
                        pos.setY(section.minY | ChunkSection.toY(value));
                        pos.setZ(section.minZ | ChunkSection.toZ(value));
                        if (pos.distToCenterSqr(view) < distance2) {
                            consumer.accept(pos);
                        }
                    }
                }
            }
        }

        public BlockState getBlockState(int x, int y, int z) {
            long chunkIndex = blockToChunkIndex(x, z);
            Chunk chunk = chunks.get(chunkIndex);
            if (chunk == null) {
                return Blocks.AIR.defaultBlockState();
            } else {
                return chunk.getBlockState(x & 0x0F, y, z & 0x0F);
            }
        }

        public void onChunkLoaded(LevelChunk levelChunk) {
            long chunkIndex = chunkToChunkIndex(levelChunk);
            Chunk chunk = chunks.get(chunkIndex);
            if (chunk != null) {
                chunk.onChunkLoaded(this, levelChunk);
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            int x = event.pos().getX();
            int y = event.pos().getY();
            int z = event.pos().getZ();
            if (x < x1 || x >= x2 || y < y1 || y >= y2 || z < z1 || z >= z2) {
                return;
            }
            long chunkIndex = blockToChunkIndex(x, z);
            Chunk chunk = chunks.get(chunkIndex);
            if (chunk != null) {
                chunk.onBlockUpdated(event);
            }
        }

        private long blockToChunkIndex(int x, int z) {
            x = SectionPos.blockToSectionCoord(x);
            z = SectionPos.blockToSectionCoord(z);
            return ChunkPos.asLong(x, z);
        }

        private long chunkToChunkIndex(LevelChunk chunk) {
            return ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z);
        }
    }

    private static class Chunk {

        public final ChunkSection[] sections;
        private final int minX;
        private final int minY;
        private final int maxY;
        private final int minZ;
        private final int sectionMinY;

        public Chunk(int x, int minY, int maxY, int z) {
            this.minX = x;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = z;
            this.sectionMinY = minY >> 4 << 4;
            int sectionMaxY = (((maxY - 1) >> 4) + 1) << 4;
            sections = new ChunkSection[(sectionMaxY - sectionMinY) >> 4];
            for (int i = 0; i < sections.length; i++) {
                sections[i] = new ChunkSection(minX, sectionMinY + (i << 4), minZ);
            }
        }

        public BlockState getBlockState(int x, int y, int z) {
            if (y < minY || y > maxY) {
                return Blocks.AIR.defaultBlockState();
            }
            int sectionIndex = (y - sectionMinY) >> 4;
            return sections[sectionIndex].getBlockState(x, y & 0x0F, z);
        }

        public double getDistanceSqrTo(Vec3 point) {
            double dx = point.x - (minX + 8);
            double dz = point.z - (minZ + 8);
            return dx * dx + dz * dz;
        }

        public void onChunkLoaded(Entry entry, LevelChunk chunk) {
            for (int i = 0; i < sections.length; i++) {
                sections[i].onChunkLoaded(entry, chunk);
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            int y = event.pos().getY();
            if (y < minY || y >= maxY) {
                return;
            }
            int sectionIndex = (y - sectionMinY) >> 4;
            sections[sectionIndex].onBlockUpdated(event);
        }

        public void setBlockState(int x, int y, int z, BlockState state) {
            int sectionIndex = (y - sectionMinY) >> 4;
            sections[sectionIndex].setBlockState(x, y & 0x0F, z, state);
        }
    }

    private static class ChunkSection {

        private final int minX;
        private final int minY;
        private final int minZ;
        private final PalettedContainer<BlockState> states;
        private final IntArrayList missing = new IntArrayList();
        private final IntArrayList wrong = new IntArrayList();

        public ChunkSection(int x, int y, int z) {
            minX = x;
            minY = y;
            minZ = z;
            states = new PalettedContainer<>(
                    Block.BLOCK_STATE_REGISTRY,
                    Blocks.AIR.defaultBlockState(),
                    PalettedContainer.Strategy.SECTION_STATES);
        }

        public BlockState getBlockState(int x, int y, int z) {
            return states.get(x, y, z);
        }

        public double getDistanceSqrTo(Vec3 point) {
            double dx = point.x - (minX + 8);
            double dy = point.y - (minY + 8);
            double dz = point.z - (minZ + 8);
            return dx * dx + dy * dy + dz * dz;
        }

        public void onChunkLoaded(Entry entry, LevelChunk chunk) {
            missing.clear();
            wrong.clear();

            int x1 = Math.max(0, entry.x1 - minX);
            int x2 = Math.min(16, entry.x2 - minX);
            int y1 = Math.max(0, entry.y1 - minY);
            int y2 = Math.min(16, entry.y2 - minY);
            int z1 = Math.max(0, entry.z1 - minZ);
            int z2 = Math.min(16, entry.z2 - minZ);

            boolean replaceableAsAir = ConfigStore.instance.getConfig().schematicaConfig.replaceableAsAir;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = x1; x < x2; x++) {
                pos.setX(x);
                for (int y = y1; y < y2; y++) {
                    pos.setY(minY | y);
                    for (int z = z1; z < z2; z++) {
                        pos.setZ(z);
                        BlockState chunkState = chunk.getBlockState(pos);
                        BlockState finalState = states.get(x, y, z);
                        if (isMissing(chunkState, finalState)) {
                            missing.add(toIndex(x, y, z));
                        } else {
                            if (isWrong(chunkState, finalState, replaceableAsAir)) {
                                wrong.add(toIndex(x, y, z));
                            }
                        }
                    }
                }
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            BlockPos pos = event.pos();
            int x = pos.getX() & 0x0F;
            int y = pos.getY() & 0x0F;
            int z = pos.getZ() & 0x0F;
            int index = toIndex(x, y, z);
            missing.remove(index);
            wrong.remove(index);
            boolean replaceableAsAir = ConfigStore.instance.getConfig().schematicaConfig.replaceableAsAir;
            BlockState chunkState = event.state();
            BlockState finalState = states.get(x, y, z);
            if (isMissing(chunkState, finalState)) {
                missing.add(index);
            } else {
                if (isWrong(chunkState, finalState, replaceableAsAir)) {
                    wrong.add(index);
                }
            }
        }

        public void setBlockState(int x, int y, int z, BlockState state) {
            states.set(x, y, z, state); // locking can be slow?
        }

        private boolean isMissing(BlockState chunkState, BlockState finalState) {
            return chunkState.getMaterial().isReplaceable() && !finalState.isAir();
        }

        private boolean isWrong(BlockState chunkState, BlockState finalState, boolean replaceableAsAir) {
            if (replaceableAsAir) {
                return !chunkState.getMaterial().isReplaceable() && chunkState != finalState;
            } else {
                return !chunkState.isAir() && chunkState != finalState;
            }
        }

        private static int toIndex(int x, int y, int z) {
            return (x << 8) | (y << 4) | z;
        }

        private static int toX(int index) {
            return (index >> 8) & 0x0F;
        }

        private static int toY(int index) {
            return (index >> 4) & 0x0F;
        }

        private static int toZ(int index) {
            return index & 0x0F;
        }
    }
}