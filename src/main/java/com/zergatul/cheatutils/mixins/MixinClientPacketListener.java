package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ClientPacketListenerMixinInterface;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener implements ClientPacketListenerMixinInterface {

    @Shadow
    private int serverChunkRadius;

    @Override
    public int getServerChunkRadius() {
        return serverChunkRadius;
    }
}
