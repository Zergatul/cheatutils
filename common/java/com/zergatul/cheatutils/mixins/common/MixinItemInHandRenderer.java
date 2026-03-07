package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.HandsViewConfig;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {

    @Inject(at = @At("HEAD"), method = "renderPlayerArm", cancellable = true)
    private void onBeforeRenderPlayerArm(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            float inverseArmHeight,
            float attackValue,
            HumanoidArm arm,
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

    @Inject(at = @At("HEAD"), method = "renderItem", cancellable = true)
    private void onBeforeRenderItem(
            LivingEntity mob,
            ItemStack itemStack,
            ItemDisplayContext type,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            CallbackInfo info
    ) {
        HandsViewConfig config = ConfigStore.instance.getConfig().handsViewConfig;
        if (!config.enabled) {
            return;
        }
        if (!config.renderItems) {
            info.cancel();
            return;
        }

        if (type == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            poseStack.translate(config.itemsShift);
        } else {
            poseStack.translate(config.itemsShift.multiply(-1, 1, 1));
        }

        poseStack.scale(
                (float) config.itemsScale.x,
                (float) config.itemsScale.y,
                (float) config.itemsScale.z);
    }
}