package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.HandsViewConfig;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class MixinFirstPersonHandsAndItemsRenderer {

    @Inject(at = @At("HEAD"), method = "renderPlayerArm", cancellable = true)
    private void onBeforeRenderPlayerArm(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            float inverseArmHeight,
            float attackValue,
            HumanoidArm arm,
            PlayerRenderState playerState,
            CallbackInfo info
    ) {
        HandsViewConfig config = ConfigStore.instance.getConfig().handsViewConfig;
        if (!config.enabled) {
            return;
        }
        if (!config.renderArms) {
            info.cancel();
            return;
        }

        if (arm == HumanoidArm.RIGHT) {
            poseStack.translate(config.armsShift);
        } else {
            poseStack.translate(config.armsShift.multiply(-1, 1, 1));
        }

        poseStack.scale(
                (float) config.armsScale.x,
                (float) config.armsScale.y,
                (float) config.armsScale.z);
    }

    @WrapWithCondition(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"),
            require = 2)
    private boolean onBeforeRenderItem(
            ItemStackRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            int outlineColor,
            @Local(name = "arm") HumanoidArm arm
    ) {
        HandsViewConfig config = ConfigStore.instance.getConfig().handsViewConfig;
        if (!config.enabled) {
            return true;
        }
        if (!config.renderItems) {
            return false;
        }

        if (arm == HumanoidArm.RIGHT) {
            poseStack.translate(config.itemsShift);
        } else {
            poseStack.translate(config.itemsShift.multiply(-1, 1, 1));
        }

        poseStack.scale(
                (float) config.itemsScale.x,
                (float) config.itemsScale.y,
                (float) config.itemsScale.z);

        return true;
    }
}