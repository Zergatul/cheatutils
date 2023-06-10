package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PreRenderTooltipEvent;
import com.zergatul.cheatutils.helpers.MixinTooltipHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {

    @Inject(at = @At("HEAD"), method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
    private void onBeforeRenderTooltip(Font font, ItemStack p_96567_, int p_96568_, int p_96569_, CallbackInfo ci) {
        MixinTooltipHelper.currentTooltipItemStack = p_96567_;
    }

    @Inject(at = @At("TAIL"), method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
    private void onAfterRenderTooltip(Font font, ItemStack p_96567_, int p_96568_, int p_96569_, CallbackInfo ci) {
        MixinTooltipHelper.currentTooltipItemStack = null;
    }

    @Inject(
            at = @At("HEAD"),
            method = "renderTooltipInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)V",
            cancellable = true)
    private void onRenderTooltipInternal(Font font, List<ClientTooltipComponent> list, int x, int y, ClientTooltipPositioner p_262920_, CallbackInfo info) {
        if (MixinTooltipHelper.currentTooltipItemStack == null) {
            return;
        }

        if (list.isEmpty()) {
            return;
        }

        if (Events.PreRenderTooltip.trigger(new PreRenderTooltipEvent((GuiGraphics) (Object) this, MixinTooltipHelper.currentTooltipItemStack, x, y))) {
            info.cancel();
        }
    }
}