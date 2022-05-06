package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.ChunkController;
import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkCache.class)
public class MixinClientChunkCache {

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/multiplayer/ClientChunkCache;updateViewRadius(I)V")
    public void onUpdateViewRadius(int p_104417_, CallbackInfo info) {
        ChunkController.instance.syncChunks();
    }
}
