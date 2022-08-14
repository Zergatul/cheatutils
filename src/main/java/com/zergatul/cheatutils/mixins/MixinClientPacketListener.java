package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinClientPacketListenerHelper;
import com.zergatul.cheatutils.interfaces.ClientPacketListenerMixinInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener implements ClientPacketListenerMixinInterface {

    @Shadow
    private int serverChunkRadius;

    @Shadow
    protected abstract void queueLightUpdate(ClientboundForgetLevelChunkPacket p_194253_);

    @Override
    public int getServerChunkRadius() {
        return serverChunkRadius;
    }

    @Override
    public void queueLightUpdate2(ClientboundForgetLevelChunkPacket packet) {
        this.queueLightUpdate(packet);
    }

    @ModifyVariable(
            at = @At("HEAD"),
            method = "Lnet/minecraft/client/multiplayer/ClientPacketListener;handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V",
            argsOnly = true,
            ordinal = 0)
    private ClientboundLoginPacket onModifyLoginPacket(ClientboundLoginPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        return new ClientboundLoginPacket(
                packet.playerId(),
                packet.hardcore(),
                packet.gameType(),
                packet.previousGameType(),
                packet.levels(),
                packet.registryHolder(),
                packet.dimensionType(),
                packet.dimension(),
                packet.seed(),
                packet.maxPlayers(),
                mc.options.renderDistance().get(),
                mc.options.simulationDistance().get(),
                packet.reducedDebugInfo(),
                packet.showDeathScreen(),
                packet.isDebug(),
                packet.isFlat(),
                packet.lastDeathLocation());
    }

    @ModifyVariable(
            at = @At("HEAD"),
            method = "Lnet/minecraft/client/multiplayer/ClientPacketListener;handleSetChunkCacheRadius(Lnet/minecraft/network/protocol/game/ClientboundSetChunkCacheRadiusPacket;)V",
            argsOnly = true,
            ordinal = 0)
    private ClientboundSetChunkCacheRadiusPacket onModifySetChunkCacheRadiusPacket(ClientboundSetChunkCacheRadiusPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        return new ClientboundSetChunkCacheRadiusPacket(mc.options.renderDistance().get());
    }

    @ModifyArg(
            method = "Lnet/minecraft/client/multiplayer/ClientPacketListener;handleDisconnect(Lnet/minecraft/network/protocol/game/ClientboundDisconnectPacket;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/chat/Component;)V"),
            index = 0)
    private Component onDisconnect(Component component) {
        if (MixinClientPacketListenerHelper.appendDisconnectMessage == null) {
            return component;
        }

        MutableComponent replacement = MutableComponent.create(component.getContents());
        String message = MixinClientPacketListenerHelper.appendDisconnectMessage;
        MixinClientPacketListenerHelper.appendDisconnectMessage = null;
        return replacement.append("\n").append(message);
    }
}
