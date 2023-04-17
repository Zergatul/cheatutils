package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {

    @Accessor("serverChunkRadius")
    int getServerChunkRadius_CU();

    @Invoker("queueLightUpdate")
    void queueLightUpdate_CU(ClientboundForgetLevelChunkPacket packet);
}