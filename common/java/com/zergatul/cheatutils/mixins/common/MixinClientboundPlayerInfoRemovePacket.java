package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.controllers.PlayerInfoController;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(ClientboundPlayerInfoRemovePacket.class)
public abstract class MixinClientboundPlayerInfoRemovePacket {

    @Shadow
    @Final
    private List<UUID> profileIds;

    @Inject(at = @At("HEAD"), method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V")
    private void onBeforeHandle(ClientGamePacketListener listener, CallbackInfo info) {
        PlayerInfoController.instance.onBeforeUpdate(this.profileIds.stream());
    }

    @Inject(at = @At("TAIL"), method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V")
    private void onAfterHandle(ClientGamePacketListener listener, CallbackInfo info) {
        PlayerInfoController.instance.onAfterUpdate();
    }
}