package com.zergatul.cheatutils.mixins.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ReachConfig;
import com.zergatul.cheatutils.modules.visuals.FullBright;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @ModifyConstant(method = "pick(F)V", constant = @Constant(doubleValue = 9.0))
    private double onModifyEntityPickRange(double constant) {
        ReachConfig config = ConfigStore.instance.getConfig().reachConfig;
        if (config.overrideAttackRange) {
            return config.attackRange * config.attackRange;
        } else {
            return constant;
        }
    }

    @Inject(at = @At("HEAD"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void onBeforeRenderLevel(float partialTicks, long limitTime, PoseStack poseStack, CallbackInfo info) {
        FullBright.instance.shouldReturnNightVisionEffect = true;
    }

    @Inject(at = @At("TAIL"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void onAfterRenderLevel(float partialTicks, long limitTime, PoseStack poseStack, CallbackInfo info) {
        FullBright.instance.shouldReturnNightVisionEffect = false;
    }
}