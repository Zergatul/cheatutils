package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.FreeCamController;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @ModifyArg(
            method = "Lnet/minecraft/client/renderer/WorldRenderer;renderLevel(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;setupRender(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/culling/ClippingHelper;ZIZ)V"),
            index = 4)
    private boolean onCallSetupRender(boolean isSpectator) {
        if (FreeCamController.instance.isActive()) {
            return true;
        } else {
            return isSpectator;
        }
    }
}