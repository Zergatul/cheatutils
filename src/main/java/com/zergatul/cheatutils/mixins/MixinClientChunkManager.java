package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public abstract class MixinClientChunkManager {

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/world/ClientChunkManager;loadChunkFromPacket(IILnet/minecraft/network/PacketByteBuf;Lnet/minecraft/nbt/NbtCompound;Ljava/util/function/Consumer;)Lnet/minecraft/world/chunk/WorldChunk;")
    private void onLoadChunkFromPacket(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> info) {
        WorldChunk chunk = info.getReturnValue();
        if (chunk != null) {
            ModApiWrapper.triggerOnChunkLoaded();
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/world/ClientChunkManager;unload(II)V")
    private void onUnload(int chunkX, int chunkZ, CallbackInfo info) {
        ModApiWrapper.triggerOnChunkUnloaded();
    }
}