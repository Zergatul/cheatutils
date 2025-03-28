package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.controllers.DebugScreenController;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay {

    @Inject(at = @At("TAIL"), method = "getGameInformation()Ljava/util/List;")
    private void onGetGameInformation(CallbackInfoReturnable<List<String>> info) {
        DebugScreenController.instance.onGetGameInformation(info.getReturnValue());
    }

    @Inject(at = @At("TAIL"), method = "getSystemInformation()Ljava/util/List;")
    private void onGetSystemInformation(CallbackInfoReturnable<List<String>> info) {
        DebugScreenController.instance.onGetSystemInformation(info.getReturnValue());
    }
}