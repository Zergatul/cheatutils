package com.zergatul.cheatutils.mixins.fabric;

import com.zergatul.cheatutils.fabric.GuiGraphicsHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {

    @Inject(at = @At("HEAD"), method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
    private void onBeforeRenderTooltip1(Font font, ItemStack itemStack, int i, int j, CallbackInfo ci) {
        GuiGraphicsHelper.tooltipItemStack = itemStack;
    }

    @Inject(at = @At("TAIL"), method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
    private void onAfterRenderTooltip1(Font font, ItemStack itemStack, int i, int j, CallbackInfo ci) {
        GuiGraphicsHelper.tooltipItemStack = null;
    }

    @Inject(at = @At("HEAD"), method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
    private void onBeforeRenderTooltip2(Font font, ItemStack itemStack, int i, int j, CallbackInfo ci) {
        GuiGraphicsHelper.tooltipItemStack = itemStack;
    }

    @Inject(at = @At("TAIL"), method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
    private void onAfterRenderTooltip2(Font font, ItemStack itemStack, int i, int j, CallbackInfo ci) {
        GuiGraphicsHelper.tooltipItemStack = null;
    }
}