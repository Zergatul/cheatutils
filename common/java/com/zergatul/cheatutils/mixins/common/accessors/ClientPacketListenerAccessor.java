package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {

    @Accessor("serverChunkRadius")
    int getServerChunkRadius_CU();
}