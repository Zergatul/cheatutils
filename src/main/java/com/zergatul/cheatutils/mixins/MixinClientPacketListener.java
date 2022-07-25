package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinClientPacketListenerHelper;
import com.zergatul.cheatutils.interfaces.ClientPacketListenerMixinInterface;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener implements ClientPacketListenerMixinInterface {

    @Shadow
    private int serverChunkRadius;

    @Override
    public int getServerChunkRadius() {
        return serverChunkRadius;
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
