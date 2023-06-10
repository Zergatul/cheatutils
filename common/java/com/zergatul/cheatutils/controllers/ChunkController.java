package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ChunksConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.ClientPacketListenerAccessor;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkController {

    public static final ChunkController instance = new ChunkController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(ChunkController.class);
    private final Map<Long, Pair<Dimension, LevelChunk>> loadedChunks = new HashMap<>();
    private final Map<Long, ChunkEntry> chunksMap = new HashMap<>();
    private final List<ChunkPos> serverUnloadedChunks = new ArrayList<>();

    private ChunkController() {
        Events.ChunkLoaded.add(this::onChunkLoaded);
        Events.ChunkUnloaded.add(this::onChunkUnloaded);
        Events.ClientTickStart.add(this::onClientTickStart);
        Events.ClientPlayerLoggingOut.add(this::onPlayerLoggingOut);
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
    }

    public synchronized List<Pair<Dimension, LevelChunk>> getLoadedChunks() {
        return new ArrayList<>(loadedChunks.values());
    }

    public synchronized int getLoadedChunksCount() {
        return loadedChunks.size();
    }

    public synchronized void clear() {
        loadedChunks.clear();
    }

    private synchronized void onChunkLoaded() {
        syncChunks();
    }

    private synchronized void onChunkUnloaded() {
        syncChunks();
    }

    private synchronized void onClientTickStart() {
        if (mc.level == null) {
            return;
        }
        ClientChunkCache cache = mc.level.getChunkSource();
        if (mc.player == null) {
            serverUnloadedChunks.forEach(p -> cache.drop(p.x, p.z));
            serverUnloadedChunks.clear();
            return;
        }

        ChunkPos playerPos = mc.player.chunkPosition();
        long renderDistance2 = mc.options.getEffectiveRenderDistance();
        renderDistance2 = renderDistance2 * renderDistance2;
        for (int i = 0; i < serverUnloadedChunks.size(); i++) {
            ChunkPos chunkPos = serverUnloadedChunks.get(i);
            long dx = chunkPos.x - playerPos.x;
            long dz = chunkPos.z - playerPos.z;
            long d2 = dx * dx + dz * dz;
            if (d2 > renderDistance2) {
                serverUnloadedChunks.remove(i);
                i--;
                ((ClientPacketListener) mc.player.connection.getConnection().getPacketListener()).handleForgetLevelChunk(new ClientboundForgetLevelChunkPacket(chunkPos.x, chunkPos.z));
            }
        }
    }

    private synchronized void onPlayerLoggingOut() {
        clear();
        // trigger unload handlers???
    }

    public synchronized void syncChunks() {
        if (mc.level == null) {
            clear();
            return;
        }

        LocalPlayer player = mc.player;

        if (player == null) {
            clear();
            return;
        }

        Dimension dimension = Dimension.get(mc.level);

        chunksMap.clear();
        for (Pair<Dimension, LevelChunk> pair: loadedChunks.values()) {
            LevelChunk chunk = pair.getSecond();
            chunksMap.put(chunk.getPos().toLong(), new ChunkEntry(chunk));
        }

        loadedChunks.clear();

        var listener = (ClientPacketListenerAccessor) player.connection;
        int storageRange = Math.max(2, listener.getServerChunkRadius_CU()) + 3;
        ChunkPos chunkPos = player.chunkPosition();
        for (int dx = -storageRange; dx <= storageRange; dx++) {
            for (int dz = -storageRange; dz <= storageRange; dz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(chunkPos.x + dx, chunkPos.z + dz, false);
                if (chunk != null) {
                    long pos = chunk.getPos().toLong();
                    ChunkEntry entry = chunksMap.get(pos);
                    if (entry != null) {
                        if (entry.chunk == chunk) {
                            entry.exists = true;
                            loadedChunks.put(pos, new Pair<>(dimension, chunk));
                        } else {
                            Events.SmartChunkUnloaded.trigger(entry.chunk);
                            loadedChunks.put(pos, new Pair<>(dimension, chunk));
                            Events.SmartChunkLoaded.trigger(chunk);
                        }
                    } else {
                        loadedChunks.put(pos, new Pair<>(dimension, chunk));
                        Events.SmartChunkLoaded.trigger(chunk);
                    }
                }
            }
        }

        for (ChunkEntry entry: chunksMap.values()) {
            if (!entry.exists) {
                Events.SmartChunkUnloaded.trigger(entry.chunk);
            }
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (args.packet instanceof ClientboundBlockUpdatePacket packet) {
            processBlockUpdatePacket(packet);
        }

        if (args.packet instanceof ClientboundSectionBlocksUpdatePacket packet) {
            processSectionBlocksUpdatePacket(packet);
        }

        if (args.packet instanceof ClientboundForgetLevelChunkPacket packet) {
            if (processForgetLevelChunkPacket(packet)) {
                args.skip = true;
            }
        }
    }

    private synchronized void processBlockUpdatePacket(ClientboundBlockUpdatePacket packet) {
        LevelChunk chunk = getChunkByPos(packet.getPos());
        if (chunk != null) {
            Events.BlockUpdated.trigger(new BlockUpdateEvent(chunk, packet.getPos(), packet.getBlockState()));
        }
    }

    private synchronized void processSectionBlocksUpdatePacket(ClientboundSectionBlocksUpdatePacket packet) {
        packet.runUpdates((pos$mutable, state) -> {
            LevelChunk chunk = getChunkByPos(pos$mutable);
            if (chunk != null) {
                BlockPos pos = new BlockPos(pos$mutable.getX(), pos$mutable.getY(), pos$mutable.getZ());
                Events.BlockUpdated.trigger(new BlockUpdateEvent(chunk, pos, state));
            }
        });
    }

    private synchronized boolean processForgetLevelChunkPacket(ClientboundForgetLevelChunkPacket packet) {
        ChunksConfig config = ConfigStore.instance.getConfig().chunksConfig;
        if (config.dontUnloadChunks) {
            serverUnloadedChunks.add(new ChunkPos(packet.getX(), packet.getZ()));
            return true;
        } else {
            return false;
        }
    }

    private LevelChunk getChunkByPos(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        ClientLevel level = mc.level;
        if (level == null) {
            return null;
        }

        // thread-safe???
        return level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false);
    }

    private static class ChunkEntry {
        public LevelChunk chunk;
        public boolean exists;

        public ChunkEntry(LevelChunk chunk) {
            this.chunk = chunk;
            this.exists = false;
        }
    }
}