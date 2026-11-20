package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.PacketProcessor$ListenerAndPacket")
public abstract class MixinPacketProcessorHandle {

    @Shadow
    @Final
    private Packet<?> packet;

    @Shadow
    @Final
    private PacketListener listener;

    @Inject(
            method = "handle",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"))
    private void onBeforeHandlePacket(CallbackInfo info) {
        if (this.listener.flow() == PacketFlow.CLIENTBOUND) {
            Events.BeforeHandlePacket.trigger(this.packet);
        }
    }
}