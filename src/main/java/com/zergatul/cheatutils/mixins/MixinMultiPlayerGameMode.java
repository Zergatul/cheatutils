package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinMultiPlayerGameModeHelper;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", cancellable = true)
    private void onReleaseUsingItem(Player p_105278_, CallbackInfo info) {
        if (MixinMultiPlayerGameModeHelper.disableReleaseUsingItem) {
            info.cancel();
        }
    }
}