package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.controllers.PlayerInfoController;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public abstract class MixinClientboundPlayerInfoUpdatePacket {

    @Shadow
    @Final
    private List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

    @Inject(at = @At("HEAD"), method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V")
    private void onBeforeHandle(ClientGamePacketListener listener, CallbackInfo info) {
        PlayerInfoController.instance.onBeforeUpdate(this.entries.stream().map(ClientboundPlayerInfoUpdatePacket.Entry::profileId));
    }

    @Inject(at = @At("TAIL"), method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V")
    private void onAfterHandle(ClientGamePacketListener listener, CallbackInfo ci) {
        PlayerInfoController.instance.onAfterUpdate();
    }
}