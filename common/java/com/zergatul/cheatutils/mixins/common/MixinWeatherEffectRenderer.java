package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public abstract class MixinWeatherEffectRenderer {

    @Inject(
            at = @At("HEAD"),
            method = "extractRenderState",
            cancellable = true)
    private void onBeforeRender(Level level, int i, float f, Vec3 p_361547_, WeatherRenderState state, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().noWeatherConfig.enabled) {
            info.cancel();
        }
    }
}