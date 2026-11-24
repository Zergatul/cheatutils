package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.extensions.ConnectionExtension;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class MixinConnection implements ConnectionExtension {

    @Final
    @Shadow
    private PacketFlow receiving;

    @Shadow
    private Channel channel;

    @Unique
    private boolean bypassSendPacketEvent_CU;

    public void setBypassSendPacketEvent_CU(boolean value) {
        this.bypassSendPacketEvent_CU = value;
    }

    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"),
            cancellable = true)
    private void onBeforeSendPacket(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo info) {
        if (this.receiving != PacketFlow.CLIENTBOUND) {
            return;
        }
        if (this.bypassSendPacketEvent_CU) {
            return;
        }
        if (!ClientThread.isCurrent()) {
            return;
        }
        if (!Events.BeforeSendPacketClientThread.trigger((Connection) (Object) this, packet)) {
            info.cancel();
            if (flush) {
                channel.flush();
            }
        }
    }

    // currently not used by anything
//    @Inject(method = "doSendPacket", at = @At("HEAD"), cancellable = true)
//    private void onBeforeWritePacketNetty(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo info) {
//        if (this.receiving != PacketFlow.CLIENTBOUND) {
//            return;
//        }
//        if (!Events.BeforeSendPacketNettyThread.trigger(packet)) {
//            info.cancel();
//        }
//    }
}