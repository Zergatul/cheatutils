package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.hacks.ElytraFly;
import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager {

    @Inject(at = @At("HEAD"), method = "play", cancellable = true)
    private void onPlay(SoundInstance sound, CallbackInfo info) {
        if (sound instanceof ElytraOnPlayerSoundInstance) {
            if (!ElytraFly.instance.shouldPlaySound()) {
                info.cancel();
            }
        }
    }
}