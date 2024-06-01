package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.controllers.CoordinatesLeakProtectionController;
import com.zergatul.cheatutils.controllers.WorldDownloadController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk {

    @Inject(at = @At("TAIL"), method = "replaceWithPacketData")
    private void onAfterReplaceWithPacketData(FriendlyByteBuf buf, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfo info) {
        CoordinatesLeakProtectionController.instance.processChunk((LevelChunk) (Object) this);
        WorldDownloadController.instance.onChunkFilledFromPacket((LevelChunk) (Object) this);
    }
}