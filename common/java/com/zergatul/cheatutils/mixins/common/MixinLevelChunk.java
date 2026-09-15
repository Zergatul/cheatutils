package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.controllers.WorldDownloadController;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk {

    @Inject(at = @At("TAIL"), method = "replaceWithPacketData")
    private void onAfterReplaceWithPacketData(int chunkX, int chunkZ, ClientboundLevelChunkPacketData chunkData, CallbackInfo info) {
        WorldDownloadController.instance.onChunkFilledFromPacket((LevelChunk) (Object) this);
    }
}