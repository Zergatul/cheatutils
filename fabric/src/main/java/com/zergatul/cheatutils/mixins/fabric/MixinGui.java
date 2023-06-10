package com.zergatul.cheatutils.mixins.fabric;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.fabric.GuiOverlays;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Shadow
    private int screenWidth;

    @Shadow
    private int screenHeight;

    @Shadow
    protected abstract int getVehicleMaxHearts(LivingEntity livingEntity);

    @Shadow
    protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Shadow protected abstract Player getCameraPlayer();

    @Shadow protected abstract int getVisibleVehicleHeartRows(int i);

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V")
    private void onBeforeRender(GuiGraphics graphics, float partialTick, CallbackInfo info) {
        if (RenderWorldLastEvent.last != null) {
            Events.PreRenderGui.trigger(new RenderGuiEvent(graphics, RenderWorldLastEvent.last));
        }
    }

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V")
    private void onAfterRender(GuiGraphics graphics, float partialTick, CallbackInfo info) {
        if (RenderWorldLastEvent.last != null) {
            Events.PostRenderGui.trigger(new RenderGuiEvent(graphics, RenderWorldLastEvent.last));
        }
    }

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"),
            method = "renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V")
    private void onAfterRenderHealth(GuiGraphics graphics, CallbackInfo info) {
        // copy from original code
        Player player = this.getCameraPlayer();
        int o = this.screenHeight - 39;
        int t = o - 10;

        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        int x = this.getVehicleMaxHearts(livingEntity);
        if (x == 0) {
            t -= 10;
        }

        int y = player.getMaxAirSupply();
        int z = Math.min(player.getAirSupply(), y);
        if (player.isEyeInFluid(FluidTags.WATER) || z < y) {
            int aa = this.getVisibleVehicleHeartRows(x) - 1;
            t += aa * 10;
        }

        GuiOverlays.render(graphics, this.screenWidth, this.screenHeight, t);
    }
}