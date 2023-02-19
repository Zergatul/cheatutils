package com.zergatul.cheatutils.controllers;

import com.mojang.serialization.Codec;
import com.zergatul.cheatutils.chunkoverlays.WorldDownloadChunkOverlay;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.storage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldDownloadController {

    public static final WorldDownloadController instance = new WorldDownloadController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(WorldDownloadController.class);
    private final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC =
            PalettedContainer.codecRW(
                    Block.BLOCK_STATE_REGISTRY,
                    BlockState.CODEC,
                    PalettedContainer.Strategy.SECTION_STATES,
                    Blocks.AIR.defaultBlockState());
    private volatile Map<ResourceKey<Level>, ChunkStorage> chunkStorages;
    private LevelStorageSource.LevelStorageAccess access;

    private final Object loopWaitEvent = new Object();
    private volatile boolean stopRequested;
    private volatile Thread thread;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    //private EntityStorage entityStorage;

    // net.minecraft.world.level.storage.LevelResource
    // PrimaryLevelData

    public WorldDownloadController() {
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
        //ModApiWrapper.ScannerChunkLoaded.add(this::onChunkLoaded);
    }

    public boolean isActive() {
        return chunkStorages != null;
    }

    public void start(String name) throws Exception {
        stop();

        File file = new File("./saves/" + name + "/level.dat");
        if (!file.exists()) {
            throw new IllegalStateException("World [" + name + "] doesn't exist in [saves] directory.");
        }

        /*entityStorage = new EntityStorage(
                null, // ServerLevel
                Path.of("C:\\Users\\Zergatul\\source\\repos\\cheatutils-1.19.3-forge\\run\\saves\\dl-test\\entities"),
                null, // data fixer
                true, // sync
                this::execute);*/

        try {
            /*WorldOpenFlows worldOpenFlows = mc.createWorldOpenFlows();
            WorldOpenFlowsMixinInterface worldOpenFlows2 = (WorldOpenFlowsMixinInterface) worldOpenFlows;
            // copy from WorldOpenFlows.doLoadLevel
            LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = mc.getLevelSource().createAccess(name);
            PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource$levelstorageaccess);
            levelstoragesource$levelstorageaccess.readAdditionalLevelSaveData();
            WorldStem worldstem = worldOpenFlows2.loadWorldStem2(levelstoragesource$levelstorageaccess, false, packrepository);
            if (worldstem.worldData() instanceof PrimaryLevelData pld) {
                pld.withConfirmedWarning(true);
            }*/

            stopRequested = false;
            access = mc.getLevelSource().createAccess(name);
            thread = new Thread(this::threadFunc, "WorldDownloadChunkSaveThread");
            thread.start();
            chunkStorages = new HashMap<>();

            ChunkOverlayController.instance.ofType(WorldDownloadChunkOverlay.class).onEnabledChanged();
        }
        catch (Throwable e) {
            stop();
            throw e;
        }
    }

    public void stop() {
        if (thread != null) {
            stopRequested = true;
            try {
                thread.join(1000);
            }
            catch (InterruptedException e) {
                thread.interrupt();
            }
            thread = null;
        }

        queue.clear();

        if (chunkStorages != null) {
            for (ChunkStorage storage : chunkStorages.values()) {
                try {
                    storage.flushWorker();
                    storage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mc.player != null && access != null) {
            PlayerDataStorage playerDataStorage = access.createPlayerStorage();
            playerDataStorage.save(mc.player);
        }

        chunkStorages = null;

        closeAccess();

        ChunkOverlayController.instance.ofType(WorldDownloadChunkOverlay.class).onEnabledChanged();
    }

    private void threadFunc() {
        try {
            while (!stopRequested) {
                synchronized (loopWaitEvent) {
                    loopWaitEvent.wait();
                }
                while (queue.size() > 0) {
                    queue.poll().run();
                }
            }
        } catch (InterruptedException e) {
            // do nothing
        } catch (Throwable e) {
            e.printStackTrace();
            stop();
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (args.packet instanceof ClientboundLevelChunkWithLightPacket packet) {
            processChunkPacket(packet.getX(), packet.getZ(), packet.getChunkData());
        }
    }

    private void processChunkPacket(int x, int z, ClientboundLevelChunkPacketData packet) {
        queue.add(() -> {
            ClientLevel level = mc.level;
            if (level == null) {
                return;
            }

            Map<ResourceKey<Level>, ChunkStorage> storages = chunkStorages;
            if (storages == null) {
                return;
            }

            Dimension dimension = Dimension.get(level);
            ResourceKey<Level> levelDimension = level.dimension();
            ChunkStorage storage;
            if (storages.containsKey(levelDimension)) {
                storage = storages.get(levelDimension);
            } else {
                storage = new ChunkStorage(
                        access.getDimensionPath(levelDimension).resolve("region"),
                        null, // data fixer
                        true); // sync
                storages.put(levelDimension, storage);
            }

            LevelChunk chunk = new LevelChunk(level, new ChunkPos(x, z));
            chunk.replaceWithPacketData(packet.getReadBuffer(), packet.getHeightmaps(), packet.getBlockEntitiesTagsConsumer(x, z));
            CompoundTag compoundtag = write(mc.level, chunk);
            storage.write(chunk.getPos(), compoundtag);

            ChunkOverlayController.instance.ofType(WorldDownloadChunkOverlay.class).notifyChunkSaved(dimension, x, z);
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    // copied from ChunkSerializer.write
    private CompoundTag write(ClientLevel level, ChunkAccess chunk) {
        ChunkPos chunkpos = chunk.getPos();
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        compoundtag.putInt("xPos", chunkpos.x);
        compoundtag.putInt("yPos", chunk.getMinSection());
        compoundtag.putInt("zPos", chunkpos.z);
        compoundtag.putLong("LastUpdate", level.getGameTime());
        compoundtag.putLong("InhabitedTime", chunk.getInhabitedTime());
        compoundtag.putString("Status", chunk.getStatus().getName());

        LevelChunkSection[] alevelchunksection = chunk.getSections();
        ListTag listtag = new ListTag();
        LevelLightEngine levellightengine = level.getChunkSource().getLightEngine();
        Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(registry);
        boolean flag = chunk.isLightCorrect();

        for(int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
            int j = chunk.getSectionIndexFromSectionY(i);
            boolean flag1 = j >= 0 && j < alevelchunksection.length;
            DataLayer datalayer = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, i));
            DataLayer datalayer1 = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, i));
            if (flag1 || datalayer != null || datalayer1 != null) {
                CompoundTag compoundtag1 = new CompoundTag();
                if (flag1) {
                    LevelChunkSection levelchunksection = alevelchunksection[j];
                    compoundtag1.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelchunksection.getStates()).getOrThrow(false, logger::error));
                    compoundtag1.put("biomes", codec.encodeStart(NbtOps.INSTANCE, levelchunksection.getBiomes()).getOrThrow(false, logger::error));
                }

                if (datalayer != null && !datalayer.isEmpty()) {
                    compoundtag1.putByteArray("BlockLight", datalayer.getData());
                }

                if (datalayer1 != null && !datalayer1.isEmpty()) {
                    compoundtag1.putByteArray("SkyLight", datalayer1.getData());
                }

                if (!compoundtag1.isEmpty()) {
                    compoundtag1.putByte("Y", (byte)i);
                    listtag.add(compoundtag1);
                }
            }
        }

        compoundtag.put("sections", listtag);
        if (flag) {
            compoundtag.putBoolean("isLightOn", true);
        }

        ListTag listtag1 = new ListTag();

        for(BlockPos blockpos : chunk.getBlockEntitiesPos()) {
            CompoundTag compoundtag3 = chunk.getBlockEntityNbtForSaving(blockpos);
            if (compoundtag3 != null) {
                listtag1.add(compoundtag3);
            }
        }

        compoundtag.put("block_entities", listtag1);
        if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
            ProtoChunk protochunk = (ProtoChunk)chunk;
            ListTag listtag2 = new ListTag();
            listtag2.addAll(protochunk.getEntities());
            compoundtag.put("entities", listtag2);
            compoundtag.put("Lights", ChunkSerializer.packOffsets(protochunk.getPackedLights()));
            CompoundTag compoundtag4 = new CompoundTag();

            for(GenerationStep.Carving generationstep$carving : GenerationStep.Carving.values()) {
                CarvingMask carvingmask = protochunk.getCarvingMask(generationstep$carving);
                if (carvingmask != null) {
                    compoundtag4.putLongArray(generationstep$carving.toString(), carvingmask.toArray());
                }
            }

            compoundtag.put("CarvingMasks", compoundtag4);
        }
        else {
            LevelChunk levelChunk = (LevelChunk) chunk;
            try {
                final CompoundTag capTag = levelChunk.writeCapsToNBT();
                if (capTag != null) compoundtag.put("ForgeCaps", capTag);
            } catch (Exception exception) {
                logger.error("A capability provider has thrown an exception trying to write state. It will not persist. Report this to the mod author", exception);
            }
        }

        saveTicks(level, compoundtag, chunk.getTicksForSerialization());
        compoundtag.put("PostProcessing", ChunkSerializer.packOffsets(chunk.getPostProcessing()));
        CompoundTag compoundtag2 = new CompoundTag();

        for(Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) {
            if (chunk.getStatus().heightmapsAfter().contains(entry.getKey())) {
                compoundtag2.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
            }
        }

        compoundtag.put("Heightmaps", compoundtag2);
        compoundtag.put("structures", packStructureData());
        return compoundtag;
    }

    private Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> p_188261_) {
        return PalettedContainer.codecRO(p_188261_.asHolderIdMap(), p_188261_.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, p_188261_.getHolderOrThrow(Biomes.PLAINS));
    }

    private void closeAccess() {
        if (access != null) {
            try {
                access.close();
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
            access = null;
        }
    }

    private static void saveTicks(ClientLevel p_188236_, CompoundTag p_188237_, ChunkAccess.TicksToSave p_188238_) {
        long i = p_188236_.getLevelData().getGameTime();
        p_188237_.put("block_ticks", p_188238_.blocks().save(i, (p_258987_) -> {
            return BuiltInRegistries.BLOCK.getKey(p_258987_).toString();
        }));
        p_188237_.put("fluid_ticks", p_188238_.fluids().save(i, (p_258989_) -> {
            return BuiltInRegistries.FLUID.getKey(p_258989_).toString();
        }));
    }

    private static CompoundTag packStructureData() {
        CompoundTag compoundtag = new CompoundTag();
        CompoundTag compoundtag1 = new CompoundTag();

        compoundtag.put("starts", compoundtag1);
        CompoundTag compoundtag2 = new CompoundTag();

        compoundtag.put("References", compoundtag2);
        return compoundtag;
    }
}