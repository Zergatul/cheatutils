package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.interfaces.ClientPlayNetHandlerMixinInterface;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.utils.TriConsumer;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SMultiBlockChangePacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ChunkController {

    public static final ChunkController instance = new ChunkController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(ChunkController.class);
    private final Map<Long, Pair<Dimension, Chunk>> loadedChunks = new HashMap<>();
    private final Map<Long, ChunkEntry> chunksMap = new HashMap<>();
    private final List<BiConsumer<Dimension, Chunk>> onChunkLoadedHandlers = new ArrayList<>();
    private final List<BiConsumer<Dimension, Chunk>> onChunkUnLoadedHandlers = new ArrayList<>();
    private final List<TriConsumer<Dimension, BlockPos, BlockState>> onBlockChangedHandlers = new ArrayList<>();

    private ChunkController() {
        ModApiWrapper.addOnChunkLoaded(this::onChunkLoaded);
        ModApiWrapper.addOnChunkUnloaded(this::onChunkUnloaded);
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
    }

    public synchronized void addOnChunkLoadedHandler(BiConsumer<Dimension, Chunk> handler) {
        onChunkLoadedHandlers.add(handler);
    }

    public synchronized void addOnChunkUnLoadedHandler(BiConsumer<Dimension, Chunk> handler) {
        onChunkUnLoadedHandlers.add(handler);
    }

    public synchronized void addOnBlockChangedHandler(TriConsumer<Dimension, BlockPos, BlockState> handler) {
        onBlockChangedHandlers.add(handler);
    }

    public synchronized List<Pair<Dimension, Chunk>> getLoadedChunks() {
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

    public synchronized void syncChunks() {
        if (mc.level == null) {
            clear();
            return;
        }

        ClientPlayerEntity player = mc.player;

        if (player == null) {
            clear();
            return;
        }

        Dimension dimension = Dimension.get(mc.level);

        chunksMap.clear();
        for (Pair<Dimension, Chunk> pair: loadedChunks.values()) {
            Chunk chunk = pair.getSecond();
            chunksMap.put(chunk.getPos().toLong(), new ChunkEntry(chunk));
        }

        loadedChunks.clear();

        ClientPlayNetHandlerMixinInterface listener = (ClientPlayNetHandlerMixinInterface) player.connection;
        int storageRange = Math.max(2, listener.getServerChunkRadius()) + 3;
        ChunkPos chunkPos = new ChunkPos(player.blockPosition());
        for (int dx = -storageRange; dx <= storageRange; dx++) {
            for (int dz = -storageRange; dz <= storageRange; dz++) {
                Chunk chunk = mc.level.getChunkSource().getChunk(chunkPos.x + dx, chunkPos.z + dz, false);
                if (chunk != null) {
                    long pos = chunk.getPos().toLong();
                    ChunkEntry entry = chunksMap.get(pos);
                    if (entry != null) {
                        if (entry.chunk == chunk) {
                            entry.exists = true;
                            loadedChunks.put(pos, new Pair<>(dimension, chunk));
                        } else {
                            invokeChunkUnloadHandlers(dimension, entry.chunk);
                            loadedChunks.put(pos, new Pair<>(dimension, chunk));
                            invokeChunkLoadHandlers(dimension, chunk);
                        }
                    } else {
                        loadedChunks.put(pos, new Pair<>(dimension, chunk));
                        invokeChunkLoadHandlers(dimension, chunk);
                    }
                }
            }
        }

        for (ChunkEntry entry: chunksMap.values()) {
            if (!entry.exists) {
                invokeChunkUnloadHandlers(dimension, entry.chunk);
            }
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (args.packet instanceof SChangeBlockPacket) {
            processBlockUpdatePacket((SChangeBlockPacket) args.packet);
        }

        if (args.packet instanceof SMultiBlockChangePacket) {
            processSectionBlocksUpdatePacket((SMultiBlockChangePacket) args.packet);
        }
    }

    private synchronized void processBlockUpdatePacket(SChangeBlockPacket packet) {
        Dimension dimension = Dimension.get(mc.level);
        for (TriConsumer<Dimension, BlockPos, BlockState> handler: onBlockChangedHandlers) {
            handler.accept(dimension, packet.getPos(), packet.getBlockState());
        }
    }

    private synchronized void processSectionBlocksUpdatePacket(SMultiBlockChangePacket packet) {
        Dimension dimension = Dimension.get(mc.level);
        packet.runUpdates((pos$mutable, state) -> {
            BlockPos pos = new BlockPos(pos$mutable.getX(), pos$mutable.getY(), pos$mutable.getZ());
            for (TriConsumer<Dimension, BlockPos, BlockState> handler: onBlockChangedHandlers) {
                handler.accept(dimension, pos, state);
            }
        });
    }

    private void invokeChunkLoadHandlers(Dimension dimension, Chunk chunk) {
        for (BiConsumer<Dimension, Chunk> handler: onChunkLoadedHandlers) {
            handler.accept(dimension, chunk);
        }
    }

    private void invokeChunkUnloadHandlers(Dimension dimension, Chunk chunk) {
        for (BiConsumer<Dimension, Chunk> handler: onChunkUnLoadedHandlers) {
            handler.accept(dimension, chunk);
        }
    }

    private static class ChunkEntry {
        public Chunk chunk;
        public boolean exists;

        public ChunkEntry(Chunk chunk) {
            this.chunk = chunk;
            this.exists = false;
        }
    }
}