package com.zergatul.cheatutils.interfaces;

import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;

public interface ClientPlayNetworkHandlerMixinInterface {
    int getServerChunkRadius();
    void unloadChunk2(UnloadChunkS2CPacket packet);
}