package com.zergatul.cheatutils.interfaces;

import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;

public interface ClientPacketListenerMixinInterface {
    int getServerChunkRadius();
    void queueLightUpdate2(ClientboundForgetLevelChunkPacket packet);
}
