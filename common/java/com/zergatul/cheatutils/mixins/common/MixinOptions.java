package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ChunksConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.helpers.MixinOptionsHelper;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class MixinOptions {

    @Shadow
    @Final
    private OptionInstance<Integer> renderDistance;

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/Options;load()V")
    private void onLoad(CallbackInfo info) {
        MixinOptionsHelper.onOptionsLoad.forEach(Runnable::run);
        MixinOptionsHelper.onOptionsLoad.clear();
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Options;getEffectiveRenderDistance()I", cancellable = true)
    private void onGetEffectiveRenderDistance(CallbackInfoReturnable<Integer> info) {
        ChunksConfig config = ConfigStore.instance.getConfig().chunksConfig;
        if (config.ignoreServerViewDistance) {
            info.setReturnValue(this.renderDistance.get());
        }
    }

    @Inject(at = @At("HEAD"), method = "bobView()Lnet/minecraft/client/OptionInstance;", cancellable = true)
    private void onBobView(CallbackInfoReturnable<OptionInstance<Boolean>> info) {
        if (FreeCam.instance.isActive()) {
            info.setReturnValue(OptionInstance.createBoolean("", false));
        }
    }
}