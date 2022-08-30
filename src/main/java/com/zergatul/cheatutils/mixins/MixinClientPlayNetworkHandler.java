package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ClientPlayNetworkHandlerMixinInterface;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler implements ClientPlayNetworkHandlerMixinInterface {

    @Shadow
    private int chunkLoadDistance;

    @Shadow
    protected abstract void unloadChunk(UnloadChunkS2CPacket packet);

    @Override
    public int getServerChunkRadius() {
        return chunkLoadDistance;
    }

    @Override
    public void unloadChunk2(UnloadChunkS2CPacket packet) {
        unloadChunk(packet);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;playerEntityId()I"), method = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V")
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        ModApiWrapper.triggerOnClientPlayerLoggingIn(MinecraftClient.getInstance().getNetworkHandler().getConnection());
    }
}