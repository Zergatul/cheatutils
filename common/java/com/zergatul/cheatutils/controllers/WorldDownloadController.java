package com.zergatul.cheatutils.controllers;

import com.mojang.serialization.Codec;
import com.zergatul.cheatutils.chunkoverlays.WorldDownloadChunkOverlay;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.mixins.common.accessors.RegionalFileStorageAccessor;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

import net.minecraft.world.level.storage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WorldDownloadController {

    public static final WorldDownloadController instance = new WorldDownloadController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(WorldDownloadController.class);
    private Map<ResourceKey<Level>, RegionFileStorage> storages;
    private LevelStorageSource.LevelStorageAccess access;

    public WorldDownloadController() {}

    public boolean isActive() {
        return storages != null;
    }

    public void start(String name) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ClientTickEndExecutor.instance.execute(() -> {
            try {
                stopInternal();

                File file = new File("./saves/" + name + "/level.dat");
                if (!file.exists()) {
                    throw new IllegalStateException("World [" + name + "] doesn't exist in [saves] directory.");
                }

                access = mc.getLevelSource().createAccess(name);
                storages = new HashMap<>();
                ChunkOverlayController.instance.ofType(WorldDownloadChunkOverlay.class).onEnabledChanged();
            } catch (Throwable e) {
                logger.error("Cannot start World Download", e);
                stopInternal();
            } finally {
                future.complete(null);
            }
        });

        future.join();
    }

    public void stop() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ClientTickEndExecutor.instance.execute(() -> {
            try {
                stopInternal();
            } finally {
                future.complete(null);
            }
        });

        future.join();
    }

    public void onChunkFilledFromPacket(LevelChunk chunk) {
        if (isActive()) {
            processChunk(chunk);
        }
    }

    private void stopInternal() {
        try {
            if (storages != null) {
                for (RegionFileStorage storage : storages.values()) {
                    try {
                        // storage.flushWorker(); // needed?
                        storage.close();
                    } catch (IOException e) {
                        logger.error("Cannot save ChunkStorage", e);
                    }
                }
            }

            if (mc.player != null && access != null) {
                PlayerDataStorage playerDataStorage = access.createPlayerStorage();
                playerDataStorage.save(mc.player);
            }

            storages = null;

            closeAccess();

            ChunkOverlayController.instance.ofType(WorldDownloadChunkOverlay.class).onEnabledChanged();
        } catch (Throwable e) {
            logger.error("Cannot stop World Download", e);
        }
    }

    private void processChunk(LevelChunk chunk) {
        try {
            ClientLevel level = (ClientLevel) chunk.getLevel();
            Dimension dimension = Dimension.get(level);
            ResourceKey<Level> levelDimension = level.dimension();

            RegionFileStorage storage;
            if (storages.containsKey(levelDimension)) {
                storage = storages.get(levelDimension);
            } else {
                storage = new RegionFileStorage(
                        new RegionStorageInfo(access.getLevelId(), levelDimension, "chunk"),
                        access.getDimensionPath(levelDimension).resolve("region"),
                        true); // sync
                storages.put(levelDimension, storage);
            }

            CompoundTag compoundtag = write(level, chunk);
            ((RegionalFileStorageAccessor) (Object) storage).write_CU(chunk.getPos(), compoundtag);

            ChunkOverlayController.instance.ofType(WorldDownloadChunkOverlay.class).notifyChunkSaved(
                    dimension, chunk.getPos().x(), chunk.getPos().z());
        } catch (Throwable e) {
            logger.error("Cannot save chunk", e);
        }
    }

    // copy from SerializableChunkData.write(level, chunk)
    private CompoundTag write(ClientLevel level, ChunkAccess chunk) {
        CompoundTag chunkNbt = NbtUtils.addCurrentDataVersion(new CompoundTag());
        chunkNbt.putInt("xPos", chunk.getPos().x());
        chunkNbt.putInt("yPos", chunk.getMinSectionY());
        chunkNbt.putInt("zPos", chunk.getPos().z());
        chunkNbt.putLong("LastUpdate", level.getGameTime());
        chunkNbt.putLong("InhabitedTime", chunk.getInhabitedTime());
        chunkNbt.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(chunk.getPersistedStatus()).toString());

        Codec<PalettedContainer<BlockState>> blockStateCodec = level.palettedContainerFactory().blockStatesContainerCodec();
        Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec = level.palettedContainerFactory().biomeContainerCodec();

        ListTag sectionsNbt = new ListTag();
        for (int i = 0; i < chunk.getSectionsCount(); i++) {
            LevelChunkSection section = chunk.getSection(i);
            CompoundTag sectionNbt = new CompoundTag();
            sectionNbt.store("block_states", blockStateCodec, section.getStates());
            sectionNbt.store("biomes", biomeCodec, section.getBiomes());
            sectionNbt.putByte("Y", (byte)(i + chunk.getMinSectionY()));
            sectionsNbt.add(sectionNbt);
        }

        chunkNbt.put("sections", sectionsNbt);

        ListTag blockEntitiesNbt = new ListTag();
        for (BlockPos blockpos : chunk.getBlockEntitiesPos()) {
            CompoundTag blockEntityNbt = chunk.getBlockEntityNbtForSaving(blockpos, level.registryAccess());
            if (blockEntityNbt != null) {
                blockEntitiesNbt.add(blockEntityNbt);
            }
        }
        chunkNbt.put("block_entities", blockEntitiesNbt);

        return chunkNbt;
    }

    private void closeAccess() {
        if (access != null) {
            try {
                access.close();
            }
            catch (Throwable e) {
                logger.error("Cannot close LevelStorageAccess", e);
            }
            access = null;
        }
    }
}