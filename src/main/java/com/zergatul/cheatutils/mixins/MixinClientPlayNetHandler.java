package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ClientPlayNetHandlerMixinInterface;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayNetHandler.class)
public class MixinClientPlayNetHandler implements ClientPlayNetHandlerMixinInterface {

    @Shadow
    private int serverChunkRadius;

    @Override
    public int getServerChunkRadius() {
        return this.serverChunkRadius;
    }
}