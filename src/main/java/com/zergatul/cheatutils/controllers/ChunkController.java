package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.configs.ChunksConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.ClientPlayNetworkHandlerMixinInterface;
import com.zergatul.cheatutils.interfaces.LevelChunkMixinInterface;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.utils.TriConsumer;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChunkController {

    public static final ChunkController instance = new ChunkController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Map<Long, Pair<Dimension, WorldChunk>> loadedChunks = new HashMap<>();
    private final Map<Long, ChunkEntry> chunksMap = new HashMap<>();
    private final List<Consumer<WorldChunk>> onChunkLoadedHandlers = new ArrayList<>();
    private final List<Consumer<WorldChunk>> onChunkUnLoadedHandlers = new ArrayList<>();
    private final List<TriConsumer<Dimension, BlockPos, BlockState>> onBlockChangedHandlers = new ArrayList<>();
    private final List<ChunkPos> serverUnloadedChunks = new ArrayList<>();

    private ChunkController() {
        ModApiWrapper.ChunkLoaded.add(this::onChunkLoaded);
        ModApiWrapper.ChunkUnloaded.add(this::onChunkUnloaded);
        ModApiWrapper.ClientTickStart.add(this::onClientTickStart);
        ModApiWrapper.ClientPlayerLoggingOut.add(this::onPlayerLoggingOut);
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
    }

    public synchronized void addOnChunkLoadedHandler(Consumer<WorldChunk> handler) {
        onChunkLoadedHandlers.add(handler);
    }

    public synchronized void addOnChunkUnLoadedHandler(Consumer<WorldChunk> handler) {
        onChunkUnLoadedHandlers.add(handler);
    }

    public synchronized void addOnBlockChangedHandler(TriConsumer<Dimension, BlockPos, BlockState> handler) {
        onBlockChangedHandlers.add(handler);
    }

    public synchronized List<Pair<Dimension, WorldChunk>> getLoadedChunks() {
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
        if (mc.world == null) {
            return;
        }
        ClientChunkManager cache = mc.world.getChunkManager();
        if (mc.player == null) {
            serverUnloadedChunks.forEach(p -> cache.unload(p.x, p.z));
            serverUnloadedChunks.clear();
            return;
        }
        ChunkPos playerPos = mc.player.getChunkPos();
        long renderDistance2 = mc.options.getClampedViewDistance();
        renderDistance2 = renderDistance2 * renderDistance2;
        for (int i = 0; i < serverUnloadedChunks.size(); i++) {
            ChunkPos chunkPos = serverUnloadedChunks.get(i);
            long dx = chunkPos.x - playerPos.x;
            long dz = chunkPos.z - playerPos.z;
            long d2 = dx * dx + dz * dz;
            if (d2 > renderDistance2) {
                serverUnloadedChunks.remove(i);
                i--;
                cache.unload(chunkPos.x, chunkPos.z);
                ((ClientPlayNetworkHandlerMixinInterface) mc.player.networkHandler.getConnection().getPacketListener()).unloadChunk2(new UnloadChunkS2CPacket(chunkPos.x, chunkPos.z));
            }
        }
    }

    private synchronized void onPlayerLoggingOut() {
        clear();
        // trigger unload handlers???
    }

    public synchronized void syncChunks() {
        if (mc.world == null) {
            clear();
            return;
        }

        ClientPlayerEntity player = mc.player;

        if (player == null) {
            clear();
            return;
        }

        Dimension dimension = Dimension.get(mc.world);

        chunksMap.clear();
        for (Pair<Dimension, WorldChunk> pair: loadedChunks.values()) {
            WorldChunk chunk = pair.getSecond();
            chunksMap.put(chunk.getPos().toLong(), new ChunkEntry(chunk));
        }

        loadedChunks.clear();

        var listener = (ClientPlayNetworkHandlerMixinInterface) player.networkHandler;
        int storageRange = Math.max(2, listener.getServerChunkRadius()) + 3;
        ChunkPos chunkPos = player.getChunkPos();
        for (int dx = -storageRange; dx <= storageRange; dx++) {
            for (int dz = -storageRange; dz <= storageRange; dz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(chunkPos.x + dx, chunkPos.z + dz, false);
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
        if (args.packet instanceof BlockUpdateS2CPacket packet) {
            processBlockUpdatePacket(packet);
        }

        if (args.packet instanceof ChunkDeltaUpdateS2CPacket packet) {
            processSectionBlocksUpdatePacket(packet);
        }

        /*if (args.packet instanceof UnloadChunkS2CPacket packet) {
            processForgetWorldChunkPacket(packet);
            args.skip = true;
        }*/
    }

    private synchronized void processBlockUpdatePacket(BlockUpdateS2CPacket packet) {
        Dimension dimension = Dimension.get(mc.world);
        for (var handler: onBlockChangedHandlers) {
            handler.accept(dimension, packet.getPos(), packet.getState());
        }
    }

    private synchronized void processSectionBlocksUpdatePacket(ChunkDeltaUpdateS2CPacket packet) {
        Dimension dimension = Dimension.get(mc.world);
        packet.visitUpdates((pos$mutable, state) -> {
            BlockPos pos = new BlockPos(pos$mutable.getX(), pos$mutable.getY(), pos$mutable.getZ());
            for (var handler: onBlockChangedHandlers) {
                handler.accept(dimension, pos, state);
            }
        });
    }

    private synchronized void processForgetWorldChunkPacket(UnloadChunkS2CPacket packet) {
        ChunksConfig config = ConfigStore.instance.getConfig().chunksConfig;
        if (config.dontUnloadChunks) {
            serverUnloadedChunks.add(new ChunkPos(packet.getX(), packet.getZ()));
        }
    }

    private void invokeChunkLoadHandlers(Dimension dimension, WorldChunk chunk) {
        ((LevelChunkMixinInterface) chunk).onLoad();
        for (Consumer<WorldChunk> handler: onChunkLoadedHandlers) {
            handler.accept(chunk);
        }
    }

    private void invokeChunkUnloadHandlers(Dimension dimension,WorldChunk chunk) {
        for (Consumer<WorldChunk> handler: onChunkUnLoadedHandlers) {
            handler.accept(chunk);
        }
    }

    private static class ChunkEntry {
        public WorldChunk chunk;
        public boolean exists;

        public ChunkEntry(WorldChunk chunk) {
            this.chunk = chunk;
            this.exists = false;
        }
    }
}
