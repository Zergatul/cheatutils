package com.zergatul.cheatutils.mixins.common.compatibility.iris;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HandRenderer.class)
public abstract class MixinHandRenderer {

    @Redirect(
            method = "canRender",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z"))
    private boolean onIsCameraDetached(Camera camera) {
        return FreeCam.instance.isCameraDetached(camera.isDetached());
    }
}