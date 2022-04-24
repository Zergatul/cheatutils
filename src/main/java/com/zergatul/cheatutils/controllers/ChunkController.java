package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChunkController {

    public static final ChunkController instance = new ChunkController();

    public final List<ChunkAccess> loadedChunks = new ArrayList<>();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<ChunkAccess> chunksToCheck = new ArrayList<>();
    private final List<Consumer<ChunkAccess>> onChunkLoadedHandlers = new ArrayList<>();
    private final List<Consumer<ChunkAccess>> onChunkUnLoadedHandlers = new ArrayList<>();
    private final List<BiConsumer<BlockPos, BlockState>> onBlockChangedHandlers = new ArrayList<>();

    private ChunkController() {
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
    }

    public void addOnChunkLoadedHandler(Consumer<ChunkAccess> handler) {
        synchronized (onChunkLoadedHandlers) {
            onChunkLoadedHandlers.add(handler);
        }
    }

    public void addOnChunkUnLoadedHandler(Consumer<ChunkAccess> handler) {
        synchronized (onChunkUnLoadedHandlers) {
            onChunkUnLoadedHandlers.add(handler);
        }
    }

    public void addOnBlockChangedHandler(BiConsumer<BlockPos, BlockState> handler) {
        synchronized (onBlockChangedHandlers) {
            onBlockChangedHandlers.add(handler);
        }
    }

    public void clear() {
        synchronized (chunksToCheck) {
            chunksToCheck.clear();
        }
        synchronized (loadedChunks) {
            loadedChunks.clear();
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        synchronized (chunksToCheck) {
            chunksToCheck.add(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        synchronized (loadedChunks) {
            loadedChunks.add(event.getChunk());
        }
        synchronized (onChunkUnLoadedHandlers) {
            for (Consumer<ChunkAccess> handler : onChunkUnLoadedHandlers) {
                handler.accept(event.getChunk());
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (mc.level == null) {
                return;
            }
            while (true) {
                ChunkAccess chunk = null;
                synchronized (chunksToCheck) {
                    if (chunksToCheck.size() > 0) {
                        chunk = chunksToCheck.remove(chunksToCheck.size() - 1);
                    }
                }
                if (chunk != null) {
                    synchronized (loadedChunks) {
                        loadedChunks.add(chunk);
                    }
                    synchronized (onChunkLoadedHandlers) {
                        for (Consumer<ChunkAccess> handler : onChunkLoadedHandlers) {
                            handler.accept(chunk);
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {

        if (args.packet instanceof ClientboundBlockUpdatePacket) {
            ClientboundBlockUpdatePacket packet = (ClientboundBlockUpdatePacket) args.packet;
            synchronized (onBlockChangedHandlers) {
                for (BiConsumer<BlockPos, BlockState> handler : onBlockChangedHandlers) {
                    handler.accept(packet.getPos(), packet.getBlockState());
                }
            }
        }

        /*if (args.packet instanceof SMultiBlockChangePacket) {
            SMultiBlockChangePacket packet = (SMultiBlockChangePacket)args.packet;
            synchronized (onBlockChangedHandlers) {
                packet.runUpdates((pos$mutable, state) -> {
                    BlockPos pos = new BlockPos(pos$mutable.getX(), pos$mutable.getY(), pos$mutable.getZ());
                    for (BiConsumer<BlockPos, BlockState> handler : onBlockChangedHandlers) {
                        handler.accept(pos, state);
                    }
                });
            }
        }*/
    }
}
