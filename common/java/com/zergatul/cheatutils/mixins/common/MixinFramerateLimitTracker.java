package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.zergatul.cheatutils.configs.PerformanceConfig;
import com.zergatul.cheatutils.modules.visuals.Performance;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public abstract class MixinFramerateLimitTracker {

    @Inject(at = @At("RETURN"), method = "getFramerateLimit", cancellable = true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> info) {
        PerformanceConfig config = Performance.INSTANCE.getSettings().get();
        if (config.limitBackgroundWindowFps && !Minecraft.getInstance().isWindowActive()) {
            info.setReturnValue(config.backgroundWindowFps);
        }
    }
}