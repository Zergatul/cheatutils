package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.controllers.DisconnectController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundDisconnectPacket.class)
public abstract class MixinClientboundDisconnectPacket {

    @Shadow
    @Final
    @Mutable
    private Component reason;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/network/chat/Component;)V")
    private void onInit(Component component, CallbackInfo info) {
        this.reason = DisconnectController.instance.appendMessage(this.reason);
    }
}