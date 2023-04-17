package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PreRenderTooltipEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class MixinScreen {

    private ItemStack currentTooltipItemStack;

    @Inject(at = @At("HEAD"), method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V")
    private void onBeforeRenderTooltip(PoseStack p_96566_, ItemStack p_96567_, int p_96568_, int p_96569_, CallbackInfo ci) {
        currentTooltipItemStack = p_96567_;
    }

    @Inject(at = @At("TAIL"), method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V")
    private void onAfterRenderTooltip(PoseStack p_96566_, ItemStack p_96567_, int p_96568_, int p_96569_, CallbackInfo ci) {
        currentTooltipItemStack = null;
    }

    @Inject(
            at = @At("HEAD"),
            method = "renderTooltipInternal(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)V",
            cancellable = true)
    private void onRenderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> list, int x, int y, ClientTooltipPositioner p_262920_, CallbackInfo info) {
        if (currentTooltipItemStack == null) {
            return;
        }

        if (list.isEmpty()) {
            return;
        }

        if (Events.PreRenderTooltip.trigger(new PreRenderTooltipEvent(poseStack, currentTooltipItemStack, x, y))) {
            info.cancel();
        }
    }
}
