package com.zergatul.cheatutils.modules.automation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.blocks.BlockPlacePlan;
import com.zergatul.cheatutils.blocks.BlockPlacer;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SchematicaConfig;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.schematics.*;
import com.zergatul.cheatutils.utils.*;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Function;

public class Schematica {

    public static final Schematica instance = new Schematica();

    private static final Strategy<BlockState> STRATEGY = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Entry> entries = new ArrayList<>();
    private final SlotSelector slotSelector = new SlotSelector();
    private volatile Long2ObjectMap<SectionInfo> lookup = new Long2ObjectOpenHashMap<>();
    private double actionTickCounter;
    private CompletableFuture<Void> applyFuture;

    private Schematica() {
        Events.RawChunkUnloaded.add(this::onChunkLoaded);
        Events.RawBlockUpdated.add(this::onBlockUpdated);
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep);
        Events.AfterRenderWorld.add(this::onRender);
    }

    public boolean isEnabled() {
        return getConfig().enabled;
    }

    public boolean isBlockRenderingEnabled() {
        SchematicaConfig config = getConfig();
        return config.enabled && config.renderBlocks;
    }

    public List<EntrySummary> getSummary() {
        CompletableFuture<List<EntrySummary>> future = new CompletableFuture<>();
        TickEndExecutor.instance.execute(() -> {
            future.complete(entries.stream().map(Entry::asSummary).toList());
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    public BlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        if (mc.level == null || mc.level.isOutsideBuildHeight(y)) {
            return Blocks.AIR.defaultBlockState();
        }

        long index = SectionPos.asLong(
                SectionPos.blockToSectionCoord(x),
                SectionPos.blockToSectionCoord(y),
                SectionPos.blockToSectionCoord(z));
        SectionInfo info = lookup.get(index);
        if (info == null) {
            return Blocks.AIR.defaultBlockState();
        }

        return info.getBlockState(x, y, z);
    }

    public SectionInfo getSectionInfo(SectionPos pos) {
        return lookup.get(pos.asLong());
    }

    public boolean hasBlocksAtSection(int x, int y, int z) {
        return hasBlocksAtSection(SectionPos.asLong(x, y, z));
    }

    public boolean hasBlocksAtSection(long index) {
        return lookup.containsKey(index);
    }

    public SchematicaSectionCopy createSectionCopy(long index) {
        SectionInfo info = lookup.get(index);
        if (info == null) {
            return SchematicaSectionCopy.EMPTY;
        }

        PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), STRATEGY);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    states.set(x, y, z, info.getBlockState(x, y, z));
                }
            }
        }

        return SchematicaSectionCopy.from(states);
    }

    public void onBlockRenderingStateChanged() {
        TickEndExecutor.instance.execute(() -> {
            boolean renderBlocks = isBlockRenderingEnabled();
            if (mc.level != null) {
                for (SectionInfo info : lookup.values()) {
                    mc.levelRenderer.setSectionDirty(info.x, info.y, info.z);
                    if (renderBlocks) {
                        mc.level.getChunkSource().onSectionEmptinessChanged(info.x, info.y, info.z, false); // hasOnlyAir=false
                    }
                }
            }
        });
    }

    public void place(SchemaFile file, String name, PlacingSettings placing) {
        TickEndExecutor.instance.execute(() -> {
            final Entry entry = new Entry(file, name, placing);
            entries.add(entry);

            AtomicReferenceArray<LevelChunk> chunks = BlockEventsProcessor.instance.getRawChunks();
            for (int i = 0; i < chunks.length(); i++) {
                LevelChunk chunk = chunks.get(i);
                if (chunk != null) {
                    entry.onChunkLoaded(chunk);
                }
            }

            safeLookupUpdate(lookup -> {
                for (Chunk chunk : entry.chunks.values()) {
                    for (ChunkSection section : chunk.sections.values()) {
                        if (section == null) {
                            continue;
                        }
                        long index = SectionPos.asLong(section.getSectionX(), section.getSectionY(), section.getSectionZ());
                        SectionInfo info = lookup.get(index);
                        if (info == null) {
                            info = SectionInfo.EMPTY;
                        }
                        lookup.put(index, info.add(entry, section));

                        if (mc.level != null) {
                            mc.levelRenderer.setSectionDirty(section.getSectionX(), section.getSectionY(), section.getSectionZ());
                            mc.level.getChunkSource().onSectionEmptinessChanged(section.getSectionX(), section.getSectionY(), section.getSectionZ(), false); // hasOnlyAir=false
                        }
                    }
                }
            });
        });
    }

    public void remove(int index) {
        TickEndExecutor.instance.execute(() -> {
            if (index < 0 || index >= entries.size()) {
                return;
            }

            safeLookupUpdate(lookup -> {
                Entry entry = entries.remove(index);
                for (Chunk chunk : entry.chunks.values()) {
                    for (ChunkSection section : chunk.sections.values()) {
                        if (section == null) {
                            continue;
                        }

                        if (mc.level != null) {
                            mc.levelRenderer.setSectionDirty(
                                    section.getSectionX(),
                                    section.getSectionY(),
                                    section.getSectionZ());
                        }

                        long sectionIndex = section.asLongIndex();
                        SectionInfo info = lookup.get(sectionIndex);
                        if (info == null) {
                            continue; // should not happen...
                        }

                        info = info.remove(entry, section);
                        if (info == SectionInfo.EMPTY) {
                            lookup.remove(sectionIndex);
                        } else {
                            lookup.put(sectionIndex, info);
                        }
                    }
                }
            });
        });
    }

    public void clear() {
        TickEndExecutor.instance.execute(() -> {
            if (mc.level != null) {
                for (SectionInfo info : lookup.values()) {
                    mc.levelRenderer.setSectionDirty(info.x, info.y, info.z);
                }
            }

            lookup = new Long2ObjectOpenHashMap<>();
            entries.clear();
        });
    }

    public void rescan(int index) {
        TickEndExecutor.instance.execute(() -> {
            if (index < 0 || index >= entries.size()) {
                return;
            }

            if (mc.level == null) {
                return;
            }

            Entry entry = entries.get(index);
            for (Chunk chunk : entry.chunks.values()) {
                for (ChunkSection section : chunk.sections.values()) {
                    if (section == null) {
                        continue;
                    }

                    // last parameter - return empty chunk, not null
                    section.onChunkLoaded(entry, mc.level.getChunkSource().getChunk(section.getSectionX(), section.getSectionZ(), true));
                    mc.levelRenderer.setSectionDirty(
                            section.getSectionX(),
                            section.getSectionY(),
                            section.getSectionZ());
                }
            }
        });
    }

    public void move(int index, int x, int y, int z) {
        TickEndExecutor.instance.execute(() -> {
            if (index < 0 || index >= entries.size()) {
                return;
            }

            safeLookupUpdate(lookup -> {
                Entry oldEntry = entries.get(index);
                for (Chunk chunk : oldEntry.chunks.values()) {
                    for (ChunkSection section : chunk.sections.values()) {
                        if (section == null) {
                            continue;
                        }

                        if (mc.level != null) {
                            mc.levelRenderer.setSectionDirty(
                                    section.getSectionX(),
                                    section.getSectionY(),
                                    section.getSectionZ());
                        }

                        long sectionIndex = section.asLongIndex();
                        SectionInfo info = lookup.get(sectionIndex);
                        if (info == null) {
                            continue; // should not happen...
                        }

                        info = info.remove(oldEntry, section);
                        if (info == SectionInfo.EMPTY) {
                            lookup.remove(sectionIndex);
                        } else {
                            lookup.put(sectionIndex, info);
                        }
                    }
                }

                Entry newEntry = oldEntry.moveTo(x, y, z);
                entries.set(index, newEntry);

                for (Chunk chunk : newEntry.chunks.values()) {
                    for (ChunkSection section : chunk.sections.values()) {
                        if (section == null) {
                            continue;
                        }

                        long chunkIndex = SectionPos.asLong(section.getSectionX(), section.getSectionY(), section.getSectionZ());
                        SectionInfo info = lookup.get(chunkIndex);
                        if (info == null) {
                            info = SectionInfo.EMPTY;
                        }
                        lookup.put(chunkIndex, info.add(newEntry, section));

                        // last parameter - return empty chunk, not null
                        if (mc.level != null) {
                            section.onChunkLoaded(newEntry, mc.level.getChunkSource().getChunk(section.getSectionX(), section.getSectionZ(), true));
                            mc.levelRenderer.setSectionDirty(
                                    section.getSectionX(),
                                    section.getSectionY(),
                                    section.getSectionZ());
                            mc.level.getChunkSource().onSectionEmptinessChanged(
                                    section.getSectionX(),
                                    section.getSectionY(),
                                    section.getSectionZ(),
                                    false); // hasOnlyAir=false
                        }
                    }
                }
            });
        });
    }

    public DownloadInfo download(String format, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (mc.level == null) {
            return DownloadInfo.of("You have to join Minecraft world");
        }

        Function<SchematicaOutputData, DownloadInfo> create;
        switch (format) {
            case "litematic" -> create = LitematicaOutputFile::create;
            case "schem-v1" -> create = SpongeSchematicaVersion1OutputFile::create;
            default -> {
                return DownloadInfo.of(String.format("Format '%s' is not supported", format));
            }
        }

        CompletableFuture<DownloadInfo> future = new CompletableFuture<>();
        TickEndExecutor.instance.execute(() -> {
            ClientChunkCache source = mc.level.getChunkSource();
            for (int x = x1; x <= x2; x += 16) {
                for (int z = z1; z < z2; z += 16) {
                    int chunkX = SectionPos.blockToSectionCoord(x);
                    int chunkZ = SectionPos.blockToSectionCoord(z);
                    if (!source.hasChunk(chunkX, chunkZ)) {
                        future.complete(DownloadInfo.of(String.format("Chunk [%d; %d] is not loaded", chunkX, chunkZ)));
                        return;
                    }
                }
            }

            List<BlockState> palette = new ArrayList<>();
            palette.add(Blocks.AIR.defaultBlockState());
            Map<BlockState, Integer> lookup = new HashMap<>();
            lookup.put(Blocks.AIR.defaultBlockState(), 0);

            int width = x2 - x1 + 1;
            int height = y2 - y1 + 1;
            int length = z2 - z1 + 1;
            int[] blocks = new int[width * height * length];
            int i = 0;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int y = y1; y <= y2; y++) {
                pos.setY(y);
                for (int z = z1; z <= z2; z++) {
                    pos.setZ(z);
                    for (int x = x1; x <= x2; x++) {
                        pos.setX(x);
                        BlockState state = mc.level.getBlockState(pos);
                        Integer index = lookup.get(state);
                        if (index == null) {
                            int newIndex = palette.size();
                            palette.add(state);
                            lookup.put(state, newIndex);
                            blocks[i++] = newIndex;
                        } else {
                            blocks[i++] = index;
                        }
                    }
                }
            }

            SchematicaOutputData data = new SchematicaOutputData(width, height, length, palette, blocks);
            future.complete(create.apply(data.optimized()));
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return DownloadInfo.of("Interrupted");
        }
    }

    private void onAfterPlayerAiStep() {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled || !config.autoBuild) {
            resetState();
            return;
        }

        if (mc.level == null || mc.player == null) {
            resetState();
            return;
        }

        actionTickCounter += 1 / config.placementRate;
        if (applyFuture != null) {
            if (applyFuture.isDone()) {
                applyFuture = null;
            } else {
                // block action is in progress
                if (actionTickCounter > 1) {
                    // don't accumulate too much while action is in progress
                    actionTickCounter = 1;
                }
                return;
            }
        }

        if (actionTickCounter >= 1) {
            actionTickCounter -= 1;

            Vec3 eyePos = mc.player.getEyePosition();
            ItemStack itemInHand = mc.player.getMainHandItem();

            Block blockInHand;
            if (itemInHand.getItem() instanceof BlockItem blockItem) {
                blockInHand = blockItem.getBlock();
            } else {
                blockInHand = null;
            }

            BlockPlacePlan plan = null;
            BlockState state = null;
            for (BlockPos pos : NearbyBlockEnumerator.getPositions(eyePos, config.maxRange)) {
                for (Entry entry : entries) {
                    state = entry.getBlockState(pos.getX(), pos.getY(), pos.getZ());
                    if (state.isAir()) {
                        continue;
                    }

                    plan = BlockPlacer.createPlan(state, pos, BlockPlacer.guessMethod(state), config);
                    if (plan != null) {
                        break;
                    }
                }

                if (plan != null) {
                    break;
                }
            }

            if (plan == null) {
                actionTickCounter = 0;
                return;
            }

            int slot = slotSelector.selectBlock(config, state.getBlock());
            if (slot >= 0) {
                mc.player.getInventory().setSelectedSlot(slot);
                blockInHand = state.getBlock();
            }
            if (blockInHand == state.getBlock()) {
                applyFuture = plan.apply();
            } else {
                actionTickCounter = 0;
            }
        }
    }

    private void onRender(RenderWorldLastEvent event) {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        Vec3 view = event.getCameraPos();

        if (config.create.enabled) {
            renderCreateBoundaries(event, view, config.create);
        }

        if (config.showMissingBlockTracers) {
            Vec3 tracerCenter = event.getTracerCenter();
            double tracerX = tracerCenter.x;
            double tracerY = tracerCenter.y;
            double tracerZ = tracerCenter.z;

            EspLineRenderer renderer = EspLineRenderer.getInstance();
            renderer.begin();

            Color color = new Color(0.2f, 1.0f, 0.2f, 0.8f);
            for (Entry entry : entries) {
                entry.forEachMissing(view, config.missingBlockTracersMaxDistance, pos -> {
                    renderer.line(
                            event.getCameraPos(),
                            tracerX, tracerY, tracerZ,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            ColorUtils.toShader(color), 1f);
                });
            }

            renderer.end(event.getMvp());
        }

        if (config.showMissingBlockCubes) {
            EspLineRenderer renderer = EspLineRenderer.getInstance();
            renderer.begin();

            Color color = new Color(0.2f, 1.0f, 0.2f, 0.8f);
            for (Entry entry : entries) {
                entry.forEachMissing(view, config.missingBlockCubesMaxDistance, pos -> {
                    double x1 = pos.getX() + 0.25;
                    double y1 = pos.getY() + 0.25;
                    double z1 = pos.getZ() + 0.25;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;
                    renderer.cuboid(event.getCameraPos(), x1, y1, z1, x2, y2, z2, ColorUtils.toShader(color), 1f);
                });
            }

            renderer.end(event.getMvp());
        }

        if (config.showWrongBlockTracers) {
            Vec3 tracerCenter = event.getTracerCenter();
            double tracerX = tracerCenter.x;
            double tracerY = tracerCenter.y;
            double tracerZ = tracerCenter.z;

            EspLineRenderer renderer = EspLineRenderer.getInstance();
            renderer.begin();

            Color color = new Color(1.0f, 0.5f, 0.5f, 0.6f);
            for (Entry entry : entries) {
                entry.forEachWrong(view, config.wrongBlockTracersMaxDistance, pos -> {
                    renderer.line(
                            event.getCameraPos(),
                            tracerX, tracerY, tracerZ,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            ColorUtils.toShader(color), 1f);
                });
            }

            renderer.end(event.getMvp());
        }

        if (config.showWrongBlockCubes) {
            EspLineRenderer renderer = EspLineRenderer.getInstance();
            renderer.begin();

            Color color = new Color(1.0f, 0.5f, 0.5f, 0.6f);
            for (Entry entry : entries) {
                entry.forEachWrong(view, config.wrongBlockCubesMaxDistance, pos -> {
                    double x1 = pos.getX() + 0.25;
                    double y1 = pos.getY() + 0.25;
                    double z1 = pos.getZ() + 0.25;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;
                    renderer.cuboid(event.getCameraPos(), x1, y1, z1, x2, y2, z2, ColorUtils.toShader(color), 1f);
                });
            }

            renderer.end(event.getMvp());
        }
    }

    private void renderCreateBoundaries(RenderWorldLastEvent event, Vec3 view, SchematicaConfig.Create create) {
        final double gap = 0.0625;

        Position3dColorRenderer quadRenderer = Position3dColorRenderer.getInstance();
        quadRenderer.begin();
        quadRenderer.cuboid(
                (float) (create.getX1() - gap - view.x),
                (float) (create.getY1() - gap - view.y),
                (float) (create.getZ1() - gap - view.z),
                (float) (create.getX2() + gap - view.x),
                (float) (create.getY2() + gap - view.y),
                (float) (create.getZ2() + gap - view.z),
                new Color(0.00f, 0.58f, 1.00f, 0.2f).getRGB());
        quadRenderer.end(event.getMvp());

        EspLineRenderer lineRenderer = EspLineRenderer.getInstance();
        lineRenderer.begin();
        lineRenderer.cuboid(
                event.getCameraPos(),
                create.getX1() - gap, create.getY1() - gap, create.getZ1() - gap,
                create.getX2() + gap, create.getY2() + gap, create.getZ2() + gap,
                ColorUtils.toShader(Color.WHITE), 1f);
        lineRenderer.end(event.getMvp(), true);
    }

    private void onChunkLoaded(LevelChunk chunk) {
        for (Entry entry : entries) {
            entry.onChunkLoaded(chunk);
        }
    }

    private void onBlockUpdated(BlockUpdateEvent event) {
        for (Entry entry : entries) {
            entry.onBlockUpdated(event);
        }
    }

    private void safeLookupUpdate(Consumer<Long2ObjectMap<SectionInfo>> consumer) {
        assert RenderSystem.isOnRenderThread();

        final Long2ObjectMap<SectionInfo> copy = new Long2ObjectOpenHashMap<>(lookup);
        consumer.accept(copy);
        lookup = copy;
    }

    private void resetState() {
        actionTickCounter = 0;
        applyFuture = null;
    }

    private SchematicaConfig getConfig() {
        return ConfigStore.instance.getConfig().schematicaConfig;
    }

    private static class Entry {

        private final String name;
        public final int x1, x2, y1, y2, z1, z2;
        public final Map<Long, Chunk> chunks;

        public Entry(SchemaFile file, String name, PlacingSettings placing) {
            this.name = name;

            PlacingConverter converter = new PlacingConverter(placing, file.getWidth(), file.getHeight(), file.getLength());

            x1 = placing.x;
            x2 = x1 + converter.getWidth();
            y1 = placing.y;
            y2 = y1 + converter.getHeight();
            z1 = placing.z;
            z2 = z1 + converter.getLength();

            chunks = new HashMap<>();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < file.getWidth(); x++) {
                for (int y = 0; y < file.getHeight(); y++) {
                    for (int z = 0; z < file.getLength(); z++) {
                        BlockState state = file.getBlockState(x, y, z);
                        if (!state.isAir()) {
                            pos.set(x, y, z);
                            converter.convert(pos);
                            int wx = x1 + pos.getX();
                            int wy = y1 + pos.getY();
                            int wz = z1 + pos.getZ();
                            long chunkIndex = blockToChunkIndex(wx, wz);
                            Chunk chunk = chunks.computeIfAbsent(chunkIndex, k -> new Chunk(wx & 0xFFFFFFF0, wz & 0xFFFFFFF0));
                            chunk.setBlockState(wx & 0x0F, wy, wz & 0x0F, state);
                        }
                    }
                }
            }
        }

        private Entry(String name, int x1, int x2, int y1, int y2, int z1, int z2, Map<Long, Chunk> chunks) {
            this.name = name;
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
            this.z1 = z1;
            this.z2 = z2;
            this.chunks = chunks;
        }

        public void forEachMissing(Vec3 view, double distance, Consumer<BlockPos> consumer) {
            double chunkDistance2 = (distance + 23) * (distance + 23);
            double distance2 = distance * distance;
            for (Chunk chunk : chunks.values()) {
                if (chunk.getDistanceSqrTo(view) > chunkDistance2) {
                    continue;
                }

                for (ChunkSection section : chunk.sections.values()) {
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

        public void forEachWrong(Vec3 view, double distance, Consumer<BlockPos> consumer) {
            double chunkDistance2 = (distance + 23) * (distance + 23);
            double distance2 = distance * distance;
            for (Chunk chunk : chunks.values()) {
                if (chunk.getDistanceSqrTo(view) > chunkDistance2) {
                    continue;
                }

                for (ChunkSection section : chunk.sections.values()) {
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
                chunk.onChunkLoaded(this, levelChunk);
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            long chunkIndex = blockToChunkIndex(event.pos().getX(), event.pos().getZ());
            Chunk chunk = chunks.get(chunkIndex);
            if (chunk != null) {
                chunk.onBlockUpdated(event);
            }
        }

        public Entry moveTo(int x, int y, int z) {
            int dx = x - x1;
            int dy = y - y1;
            int dz = z - z1;
            return new Entry(
                    name,
                    x1 + dx, x2 + dx,
                    y1 + dy, y2 + dy,
                    z1 + dz, z2 + dz,
                    copyChunks(dx, dy, dz));
        }

        public EntrySummary asSummary() {
            return new EntrySummary(name, x1, y1, z1);
        }

        private long blockToChunkIndex(int x, int z) {
            x = SectionPos.blockToSectionCoord(x);
            z = SectionPos.blockToSectionCoord(z);
            return ChunkPos.pack(x, z);
        }

        private long chunkToChunkIndex(LevelChunk chunk) {
            return ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z());
        }

        private Map<Long, Chunk> copyChunks(int dx, int dy, int dz) {
            Map<Long, Chunk> newChunks = new HashMap<>();
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    for (int z = z1; z < z2; z++) {
                        BlockState state = getBlockState(x, y, z);
                        if (!state.isAir()) {
                            int wx = x + dx;
                            int wy = y + dy;
                            int wz = z + dz;
                            long chunkIndex = blockToChunkIndex(wx, wz);
                            Chunk chunk = newChunks.computeIfAbsent(chunkIndex, k -> new Chunk(wx & 0xFFFFFFF0, wz & 0xFFFFFFF0));
                            chunk.setBlockState(wx & 0x0F, wy, wz & 0x0F, state);
                        }
                    }
                }
            }
            return newChunks;
        }
    }

    private static class Chunk {

        private final int minX;
        private final int minZ;
        public final Int2ObjectMap<ChunkSection> sections;

        public Chunk(int x, int z) {
            minX = x;
            minZ = z;
            sections = new Int2ObjectOpenHashMap<>();
        }

        public BlockState getBlockState(int x, int y, int z) {
            assert 0 <= x && x < 16;
            assert 0 <= z && z < 16;

            int sectionIndex = SectionPos.blockToSectionCoord(y);
            ChunkSection section = sections.get(sectionIndex);
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

        public void onChunkLoaded(Entry entry, LevelChunk chunk) {
            assert chunk.getPos().getMinBlockX() == minX;
            assert chunk.getPos().getMinBlockZ() == minZ;

            for (ChunkSection section : sections.values()) {
                section.onChunkLoaded(entry, chunk);
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            int sectionIndex = SectionPos.blockToSectionCoord(event.pos().getY());
            ChunkSection section = sections.get(sectionIndex);
            if (section != null) {
                section.onBlockUpdated(event);
            }
        }

        public void setBlockState(int x, int y, int z, BlockState state) {
            assert 0 <= x && x < 16;
            assert 0 <= z && z < 16;

            int sectionIndex = SectionPos.blockToSectionCoord(y);
            ChunkSection section = sections.get(sectionIndex);
            if (section == null) {
                section = new ChunkSection(minX, SectionPos.sectionToBlockCoord(sectionIndex), minZ);
                sections.put(sectionIndex, section);
            }
            section.setBlockState(x, y & 0x0F, z, state);
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
            states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), STRATEGY);
        }

        public int getSectionX() {
            return SectionPos.blockToSectionCoord(minX);
        }

        public int getSectionY() {
            return SectionPos.blockToSectionCoord(minY);
        }

        public int getSectionZ() {
            return SectionPos.blockToSectionCoord(minZ);
        }

        public long asLongIndex() {
            return SectionPos.asLong(getSectionX(), getSectionY(), getSectionZ());
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

            SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 16; x++) {
                int worldX = minX | x;
                if (worldX < entry.x1 || worldX >= entry.x2) {
                    continue;
                }

                pos.setX(x);
                for (int y = 0; y < 16; y++) {
                    int worldY = minY | y;
                    if (worldY < entry.y1 || worldY >= entry.y2) {
                        continue;
                    }

                    pos.setY(worldY);
                    for (int z = 0; z < 16; z++) {
                        int worldZ = minZ | z;
                        if (worldZ < entry.z1 || worldZ >= entry.z2) {
                            continue;
                        }

                        pos.setZ(z);
                        BlockState chunkState = chunk.getBlockState(pos);
                        BlockState finalState = states.get(x, y, z);
                        if (isMissing(chunkState, finalState)) {
                            missing.add(new BlockPos(worldX, worldY, worldZ));
                        } else {
                            if (isWrong(chunkState, finalState, config)) {
                                wrong.add(new BlockPos(worldX, worldY, worldZ));
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
            BlockState chunkState = event.state();
            BlockState finalState = states.get(pos.getX() & 0x0F, pos.getY() & 0x0F, pos.getZ() & 0x0F);
            if (isMissing(chunkState, finalState)) {
                missing.add(pos.immutable());
            } else {
                if (isWrong(chunkState, finalState, ConfigStore.instance.getConfig().schematicaConfig)) {
                    wrong.add(pos.immutable());
                }
            }
        }

        public void setBlockState(int x, int y, int z, BlockState state) {
            states.set(x, y, z, state); // locking can be slow?
        }

        private boolean isMissing(BlockState chunkState, BlockState finalState) {
            return chunkState.canBeReplaced() && !finalState.isAir();
        }

        private boolean isWrong(BlockState chunkState, BlockState finalState, SchematicaConfig config) {
            if (config.airAlwaysValid && finalState.isAir()) {
                return false;
            } else if (config.replaceableAsAir) {
                return !chunkState.canBeReplaced() && chunkState != finalState;
            } else {
                return !chunkState.isAir() && chunkState != finalState;
            }
        }
    }

    public static class SectionInfo {

        public static final SectionInfo EMPTY = new SectionInfo();

        private final int x, y, z;
        private final List<Entry> entries;
        private final List<ChunkSection> sections;
        private final Entry entry;
        private final ChunkSection section;

        private SectionInfo() {
            this.x = this.y = this.z = 0;
            this.entries = null;
            this.sections = null;
            this.entry = null;
            this.section = null;
        }

        private SectionInfo(Entry entry, ChunkSection section) {
            this.x = section.getSectionX();
            this.y = section.getSectionY();
            this.z = section.getSectionZ();
            this.entries = null;
            this.sections = null;
            this.entry = entry;
            this.section = section;
        }

        private SectionInfo(int x, int y, int z, List<Entry> entries, List<ChunkSection> sections) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.entries = entries;
            this.sections = sections;
            this.entry = null;
            this.section = null;
        }

        private SectionInfo(int x, int y, int z, Entry entry1, ChunkSection section1, Entry entry2, ChunkSection section2) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.entries = List.of(entry1, entry2);
            this.sections = List.of(section1, section2);
            this.entry = null;
            this.section = null;
        }

        private SectionInfo(int x, int y, int z, List<Entry> entries, List<ChunkSection> sections, Entry entry, ChunkSection section) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.entries = new ArrayList<>(entries.size() + 1);
            this.entries.addAll(entries);
            this.entries.add(entry);
            this.sections = new ArrayList<>(sections.size() + 1);
            this.sections.addAll(sections);
            this.sections.add(section);
            this.entry = null;
            this.section = null;
        }

        private SectionInfo add(Entry entry, ChunkSection section) {
            if (this.entry == null && this.entries == null) {
                return new SectionInfo(entry, section);
            }
            if (this.entry != null) {
                return new SectionInfo(this.x, this.y, this.z, this.entry, this.section, entry, section);
            }
            return new SectionInfo(this.x, this.y, this.z, this.entries, this.sections, entry, section);
        }

        private SectionInfo remove(Entry entry, ChunkSection section) {
            if (this.entry != null) {
                if (this.entry != entry || this.section != section) {
                    throw new IllegalStateException("Attempt to remove non-existing section.");
                }
                return EMPTY;
            }
            if (this.entries != null) {
                int index = this.entries.indexOf(entry);
                if (index < 0) {
                    throw new IllegalStateException("Attempt to remove non-existing section.");
                }
                if (this.sections.get(index) != section) {
                    throw new IllegalStateException("Entry/ChunkSection mismatch.");
                }
                List<Entry> entries = removeAtIndex(this.entries, index);
                List<ChunkSection> sections = removeAtIndex(this.sections, index);
                if (entries.size() == 1) {
                    return new SectionInfo(entries.getFirst(), sections.getFirst());
                } else {
                    return new SectionInfo(x, y, z, entries, sections);
                }
            }
            throw new IllegalStateException();
        }

        public boolean contains(BlockPos pos) {
            return SectionPos.asLong(pos) == SectionPos.asLong(x, y, z);
        }

        public BlockState getBlockState(BlockPos pos) {
            return getBlockState(pos.getX(), pos.getY(), pos.getZ());
        }

        public BlockState getBlockState(int x, int y, int z) {
            x &= 0xF;
            y &= 0xF;
            z &= 0xF;

            if (section != null) {
                return section.getBlockState(x, y, z);
            }
            for (ChunkSection section : sections) {
                BlockState state = section.getBlockState(x, y, z);
                if (!state.isAir()) {
                    return state;
                }
            }
            return Blocks.AIR.defaultBlockState();
        }

        private static <T> List<T> removeAtIndex(List<T> list, int index) {
            List<T> result = new ArrayList<>(list.size() - 1);
            for (int i = 0; i < list.size(); i++) {
                if (i != index) {
                    result.add(list.get(i));
                }
            }
            return result;
        }
    }

    public record EntrySummary(String name, int x, int y, int z) {}

    public record DownloadInfo(byte[] data, String error) {

        public static DownloadInfo of(byte[] data) {
            return new DownloadInfo(data, null);
        }

        public static DownloadInfo of(String error) {
            return new DownloadInfo(null, error);
        }
    }
}