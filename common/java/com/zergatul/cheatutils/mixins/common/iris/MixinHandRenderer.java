package com.zergatul.cheatutils.mixins.common.iris;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.irisshaders.iris.pathways.HandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HandRenderer.class)
public abstract class MixinHandRenderer {

    @ModifyMethodReturnValue(
            method = "canRender",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z"))
    private boolean onModifyIsCameraDetached(boolean value) {
        return FreeCam.instance.isCameraDetached(value);
    }
}