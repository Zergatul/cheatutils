package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SchematicaConfig;
import com.zergatul.cheatutils.render.Primitives;
import com.zergatul.cheatutils.schematics.PlacingConverter;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchemaFile;
import com.zergatul.cheatutils.utils.*;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.BlockUpdateEvent;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceInfo;
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
        ModApiWrapper.RenderWorldLast.add(this::onRender);

        Arrays.fill(lastSlotUsage, Long.MIN_VALUE);
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void place(SchemaFile file, PlacingSettings placing) {
        Entry entry = new Entry(file, placing);
        entries.add(entry);
        ChunkController.instance.getLoadedChunks().forEach(p -> entry.onChunkLoaded(p.getSecond()));
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

    private synchronized void onRender(RenderWorldLastEvent event) {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        Vec3 view = event.getCamera().getPosition();

        if (config.showMissingBlockTracers) {
            Vec3 tracerCenter = event.getTracerCenter();
            double tracerX = tracerCenter.x;
            double tracerY = tracerCenter.y;
            double tracerZ = tracerCenter.z;

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

            for (Entry entry : entries) {
                entry.forEachMissing(view, config.missingBlockTracersMaxDistance, pos -> {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                });
            }

            Primitives.renderLines(bufferBuilder, event.getMatrixStack().last().pose(), event.getProjectionMatrix());
        }

        if (config.showMissingBlockCubes) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

            for (Entry entry : entries) {
                entry.forEachMissing(view, config.missingBlockCubesMaxDistance, pos -> {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;
                    Primitives.drawCube(bufferBuilder, x1, y1, z1, x2, y2, z2);
                });
            }

            Primitives.renderLines(bufferBuilder, event.getMatrixStack().last().pose(), event.getProjectionMatrix());
        }

        if (config.showMissingBlockGhosts) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 0.6f);

            Map<BlockPos, BlockState> ghosts = new HashMap<>();
            for (Entry entry : entries) {
                entry.forEachMissingState(view, config.missingBlockGhostsMaxDistance, ghosts::put);
            }

            BlockPos.MutableBlockPos neighPos = new BlockPos.MutableBlockPos();
            for (var mapEntry : ghosts.entrySet()) {
                BlockPos pos = mapEntry.getKey();
                BlockState state = mapEntry.getValue();
                BakedModel model = mc.getBlockRenderer().getBlockModel(state);
                for (var direction : Direction.values()) {
                    neighPos.setX(pos.getX() + direction.getStepX());
                    neighPos.setY(pos.getY() + direction.getStepY());
                    neighPos.setZ(pos.getZ() + direction.getStepZ());
                    if (!ghosts.containsKey(neighPos)) {
                        List<BakedQuad> quads = model.getQuads(null, direction, random, ModelData.EMPTY, null);
                        if (quads.size() > 0) {
                            BakedQuad quad = quads.get(0);
                            TextureAtlasSprite sprite = quad.getSprite();
                            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                            RenderSystem.setShaderTexture(0, sprite.atlasLocation());

                            FaceInfo face = FaceInfo.fromFacing(direction);
                            FaceInfo.VertexInfo info;

                            info = face.getVertexInfo(0);
                            bufferBuilder.vertex(
                                            (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                            (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                            (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                    .uv(sprite.getU0(), sprite.getV0()).endVertex();

                            info = face.getVertexInfo(1);
                            bufferBuilder.vertex(
                                            (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                            (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                            (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                    .uv(sprite.getU0(), sprite.getV1()).endVertex();

                            info = face.getVertexInfo(2);
                            bufferBuilder.vertex(
                                            (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                            (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                            (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                    .uv(sprite.getU1(), sprite.getV1()).endVertex();

                            info = face.getVertexInfo(3);
                            bufferBuilder.vertex(
                                            (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                            (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                            (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                    .uv(sprite.getU1(), sprite.getV0()).endVertex();

                            SharedVertexBuffer.instance.bind();
                            SharedVertexBuffer.instance.upload(bufferBuilder.end());
                            SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionTexShader());
                            VertexBuffer.unbind();
                        }
                    }
                }
            }
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
                entry.forEachWrong(view, config.wrongBlockTracersMaxDistance, pos -> {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                });
            }

            Primitives.renderLines(bufferBuilder, event.getMatrixStack().last().pose(), event.getProjectionMatrix());
        }

        if (config.showWrongBlockCubes) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 0.6f);

            for (Entry entry : entries) {
                entry.forEachWrong(view, config.wrongBlockCubesMaxDistance, pos -> {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;
                    Primitives.drawCube(bufferBuilder, x1, y1, z1, x2, y2, z2);
                });
            }

            Primitives.renderLines(bufferBuilder, event.getMatrixStack().last().pose(), event.getProjectionMatrix());
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

        public final int x1, x2, y1, y2, z1, z2;
        public final Map<Long, Chunk> chunks;

        public Entry(SchemaFile file, PlacingSettings placing) {
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
                                chunk = new Chunk(wx & 0xFFFFFFF0, wz & 0xFFFFFFF0);
                                chunks.put(chunkIndex, chunk);
                            }
                            chunk.setBlockState(wx & 0x0F, wy, wz & 0x0F, state);
                        }
                    }
                }
            }
        }

        public void forEachMissing(Vec3 view, double distance, Consumer<BlockPos> consumer) {
            double chunkDistance2 = (distance + 23) * (distance + 23);
            double distance2 = distance * distance;
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

                    for (BlockPos pos : section.missing) {
                        if (pos.distToCenterSqr(view) < distance2) {
                            consumer.accept(pos);
                        }
                    }
                }
            }
        }

        public void forEachMissingState(Vec3 view, double distance, BiConsumer<BlockPos, BlockState> consumer) {
            double chunkDistance2 = (distance + 23) * (distance + 23);
            double distance2 = distance * distance;
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

                    for (BlockPos pos : section.missing) {
                        if (pos.distToCenterSqr(view) < distance2) {
                            consumer.accept(pos, section.getBlockState(pos.getX() & 0x0F, pos.getY() & 0x0F, pos.getZ() & 0x0F));
                        }
                    }
                }
            }
        }

        public void forEachWrong(Vec3 view, double distance, Consumer<BlockPos> consumer) {
            double chunkDistance2 = (distance + 23) * (distance + 23);
            double distance2 = distance * distance;
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

                    for (BlockPos pos : section.wrong) {
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
                chunk.onChunkLoaded(levelChunk);
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            long chunkIndex = blockToChunkIndex(event.pos().getX(), event.pos().getZ());
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

        private static final int MIN_Y = -64;
        private static final int MAX_Y = 320;
        private static final int MIN_SECTION_Y = MIN_Y >> 4;
        private static final int MAX_SECTION_Y = MAX_Y >> 4;

        private final int minX;
        private final int minZ;
        public final ChunkSection[] sections;

        public Chunk(int x, int z) {
            minX = x;
            minZ = z;
            sections = new ChunkSection[MAX_SECTION_Y - MIN_SECTION_Y];
        }

        public BlockState getBlockState(int x, int y, int z) {
            int sectionIndex = (y - MIN_Y) >> 4;
            ChunkSection section = sections[sectionIndex];
            if (section == null) {
                return Blocks.AIR.defaultBlockState();
            } else {
                return section.getBlockState(x, y & 0x0F, z);
            }
        }

        public double getDistanceSqrTo(Vec3 point) {
            double dx = point.x - (minX + 8);
            double dz = point.z - (minZ + 8);
            return dx * dx + dz * dz;
        }

        public void onChunkLoaded(LevelChunk chunk) {
            for (int i = 0; i < sections.length; i++) {
                if (sections[i] != null) {
                    sections[i].onChunkLoaded(chunk);
                }
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            if (event.pos().getY() >= 320) { // light update?
                return;
            }
            int sectionIndex = (event.pos().getY() - MIN_Y) >> 4;
            ChunkSection section = sections[sectionIndex];
            if (section != null) {
                section.onBlockUpdated(event);
            }
        }

        public void setBlockState(int x, int y, int z, BlockState state) {
            int sectionIndex = (y - MIN_Y) >> 4;
            if (sections[sectionIndex] == null) {
                sections[sectionIndex] = new ChunkSection(minX, MIN_Y + (sectionIndex << 4), minZ);
            }
            sections[sectionIndex].setBlockState(x, y & 0x0F, z, state);
        }
    }

    private static class ChunkSection {

        private final int minX;
        private final int minY;
        private final int minZ;
        private final PalettedContainer<BlockState> states;
        private final List<BlockPos> missing = new ArrayList<>();
        private final List<BlockPos> wrong = new ArrayList<>();

        public ChunkSection(int x, int y, int z) {
            minX = x;
            minY = y;
            minZ = z;
            states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
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

        public void onChunkLoaded(LevelChunk chunk) {
            missing.clear();
            wrong.clear();

            boolean replaceableAsAir = ConfigStore.instance.getConfig().schematicaConfig.replaceableAsAir;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 16; x++) {
                pos.setX(x);
                for (int y = 0; y < 16; y++) {
                    pos.setY(minY | y);
                    for (int z = 0; z < 16; z++) {
                        pos.setZ(z);
                        BlockState chunkState = chunk.getBlockState(pos);
                        BlockState finalState = states.get(x, y, z);
                        if (isMissing(chunkState, finalState)) {
                            missing.add(new BlockPos(minX | x, minY | y, minZ | z));
                        } else {
                            if (isWrong(chunkState, finalState, replaceableAsAir)) {
                                wrong.add(new BlockPos(minX | x, minY | y, minZ | z));
                            }
                        }
                    }
                }
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            BlockPos pos = event.pos();
            missing.removeIf(p -> p.equals(pos));
            wrong.removeIf(p -> p.equals(pos));
            boolean replaceableAsAir = ConfigStore.instance.getConfig().schematicaConfig.replaceableAsAir;
            BlockState chunkState = event.state();
            BlockState finalState = states.get(pos.getX() & 0x0F, pos.getY() & 0x0F, pos.getZ() & 0x0F);
            if (isMissing(chunkState, finalState)) {
                missing.add(pos.immutable());
            } else {
                if (isWrong(chunkState, finalState, replaceableAsAir)) {
                    wrong.add(pos.immutable());
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
    }
}