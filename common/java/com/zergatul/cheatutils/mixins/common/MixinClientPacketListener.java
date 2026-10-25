package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.SendChatEvent;
import com.zergatul.cheatutils.configs.modules.ChunksConfig;
import com.zergatul.cheatutils.modules.visuals.Chunks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Shadow
    public abstract Connection getConnection();

    @ModifyVariable(
            at = @At("HEAD"),
            method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V",
            argsOnly = true,
            ordinal = 0)
    private ClientboundLoginPacket onModifyLoginPacket(ClientboundLoginPacket packet) {
        ChunksConfig config = Chunks.INSTANCE.getSettings().get();
        if (config.ignoreServerViewDistance()) {
            Minecraft mc = Minecraft.getInstance();
            return new ClientboundLoginPacket(
                    packet.playerId(),
                    packet.hardcore(),
                    packet.levels(),
                    packet.maxPlayers(),
                    mc.options.renderDistance().get(),
                    mc.options.simulationDistance().get(),
                    packet.reducedDebugInfo(),
                    packet.showDeathScreen(),
                    packet.doLimitedCrafting(),
                    packet.commonPlayerSpawnInfo(),
                    packet.onlineMode(),
                    packet.enforcesSecureChat());
        } else {
            return packet;
        }
    }

    @ModifyVariable(
            at = @At("HEAD"),
            method = "handleSetChunkCacheRadius(Lnet/minecraft/network/protocol/game/ClientboundSetChunkCacheRadiusPacket;)V",
            argsOnly = true,
            ordinal = 0)
    private ClientboundSetChunkCacheRadiusPacket onModifySetChunkCacheRadiusPacket(ClientboundSetChunkCacheRadiusPacket packet) {
        ChunksConfig config = Chunks.INSTANCE.getSettings().get();
        if (config.ignoreServerViewDistance()) {
            Minecraft mc = Minecraft.getInstance();
            return new ClientboundSetChunkCacheRadiusPacket(mc.options.renderDistance().get());
        } else {
            return packet;
        }
    }

    /*@Inject(at = @At("HEAD"), method = "handleForgetLevelChunk", cancellable = true)
    private void onHandleForgetLevelChunkPacket(ClientboundForgetLevelChunkPacket packet, CallbackInfo info) {
        if (Chunks.INSTANCE.getSettings().get().dontUnloadChunks()) {
            info.cancel();
        }
    }*/

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;playerId()I"),
            method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    private void onPlayerLoggingIn(ClientboundLoginPacket packet, CallbackInfo info) {
        Events.ClientPlayerLoggingIn.trigger(this.getConnection());
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String content, CallbackInfo info) {
        if (Events.SendChat.trigger(new SendChatEvent(content))) {
            info.cancel();
        }
    }
}