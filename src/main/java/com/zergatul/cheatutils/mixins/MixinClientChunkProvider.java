package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.ChunkController;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkProvider.class)
public abstract class MixinClientChunkProvider {

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/multiplayer/ClientChunkProvider;updateViewRadius(I)V")
    public void onUpdateViewRadius(int p_104417_, CallbackInfo info) {
        ChunkController.instance.syncChunks();
    }
}
