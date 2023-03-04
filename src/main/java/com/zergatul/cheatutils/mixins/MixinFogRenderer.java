package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FogConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

    @Inject(at = @At("HEAD"), method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V", cancellable = true)
    private static void onSetupFog(Camera livingentity, FogRenderer.FogMode mobeffectinstance, float localplayer, boolean holder, float f, CallbackInfo info) {
        FogConfig config = ConfigStore.instance.getConfig().fogConfig;
        if (config.disableFog && FogConfig.METHOD_SKIP_SETUP_FOG.equals(config.method)) {
            info.cancel();
        }
    }
}