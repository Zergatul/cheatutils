package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.interfaces.ClientPacketListenerMixinInterface;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.utils.TriConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChunkController {

    public static final ChunkController instance = new ChunkController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(ChunkController.class);
    private final Map<Long, Pair<Dimension, LevelChunk>> loadedChunks = new HashMap<>();
    private final Map<Long, ChunkEntry> chunksMap = new HashMap<>();
    private final List<BiConsumer<Dimension, LevelChunk>> onChunkLoadedHandlers = new ArrayList<>();
    private final List<BiConsumer<Dimension, LevelChunk>> onChunkUnLoadedHandlers = new ArrayList<>();
    private final List<TriConsumer<Dimension, BlockPos, BlockState>> onBlockChangedHandlers = new ArrayList<>();

    private ChunkController() {
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
    }

    public synchronized void addOnChunkLoadedHandler(BiConsumer<Dimension, LevelChunk> handler) {
        onChunkLoadedHandlers.add(handler);
    }

    public synchronized void addOnChunkUnLoadedHandler(BiConsumer<Dimension, LevelChunk> handler) {
        onChunkUnLoadedHandlers.add(handler);
    }

    public synchronized void addOnBlockChangedHandler(TriConsumer<Dimension, BlockPos, BlockState> handler) {
        onBlockChangedHandlers.add(handler);
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

    @SubscribeEvent()
    public synchronized void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        syncChunks();
    }

    @SubscribeEvent
    public synchronized void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        syncChunks();
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

        var listener = (ClientPacketListenerMixinInterface) player.connection;
        int storageRange = Math.max(2, listener.getServerChunkRadius()) + 3;
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
        if (args.packet instanceof ClientboundBlockUpdatePacket) {
            processBlockUpdatePacket((ClientboundBlockUpdatePacket) args.packet);
        }

        if (args.packet instanceof ClientboundSectionBlocksUpdatePacket) {
            processSectionBlocksUpdatePacket((ClientboundSectionBlocksUpdatePacket) args.packet);
        }
    }

    private synchronized void processBlockUpdatePacket(ClientboundBlockUpdatePacket packet) {
        Dimension dimension = Dimension.get(mc.level);
        for (var handler: onBlockChangedHandlers) {
            handler.accept(dimension, packet.getPos(), packet.getBlockState());
        }
    }

    private synchronized void processSectionBlocksUpdatePacket(ClientboundSectionBlocksUpdatePacket packet) {
        Dimension dimension = Dimension.get(mc.level);
        packet.runUpdates((pos$mutable, state) -> {
            BlockPos pos = new BlockPos(pos$mutable.getX(), pos$mutable.getY(), pos$mutable.getZ());
            for (var handler: onBlockChangedHandlers) {
                handler.accept(dimension, pos, state);
            }
        });
    }

    private void invokeChunkLoadHandlers(Dimension dimension, LevelChunk chunk) {
        for (BiConsumer<Dimension, LevelChunk> handler: onChunkLoadedHandlers) {
            handler.accept(dimension, chunk);
        }
    }

    private void invokeChunkUnloadHandlers(Dimension dimension,LevelChunk chunk) {
        for (BiConsumer<Dimension, LevelChunk> handler: onChunkUnLoadedHandlers) {
            handler.accept(dimension, chunk);
        }
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
