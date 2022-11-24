package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinInGameHudHelper;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.PostRenderGuiEvent;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V")
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo info) {
        ModApiWrapper.PostRenderGui.trigger(new PostRenderGuiEvent(matrices, tickDelta));
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V")
    private void onBeforeRenderCrosshair(MatrixStack matrices, CallbackInfo info) {
        MixinInGameHudHelper.insideRenderCrosshair = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V")
    private void onAfterRenderCrosshair(MatrixStack matrices, CallbackInfo info) {
        MixinInGameHudHelper.insideRenderCrosshair = false;
    }
}