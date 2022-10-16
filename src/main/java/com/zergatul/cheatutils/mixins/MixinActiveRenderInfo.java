package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.FreeCamController;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ActiveRenderInfo.class)
public abstract class MixinActiveRenderInfo {

    @Shadow(aliases = "Lnet/minecraft/client/renderer/ActiveRenderInfo;setRotation(FF)V")
    protected abstract void setRotation(float p_90573_, float p_90574_);

    @Shadow(aliases = "Lnet/minecraft/client/renderer/ActiveRenderInfo;setPosition(DDD)V")
    protected abstract void setPosition(double p_90585_, double p_90586_, double p_90587_);

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/renderer/ActiveRenderInfo;setup(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/entity/Entity;ZZF)V", cancellable = true)
    private void onSetup(IBlockReader p_216772_1_, Entity p_216772_2_, boolean p_216772_3_, boolean p_216772_4_, float p_216772_5_, CallbackInfo info) {
        FreeCamController controller = FreeCamController.instance;
        if (FreeCamController.instance.isActive()) {
            setRotation(controller.getYRot(), controller.getXRot());
            setPosition(controller.getX(), controller.getY(), controller.getZ());
        }
    }
}