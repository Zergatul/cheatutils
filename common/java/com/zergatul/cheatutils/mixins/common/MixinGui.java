package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PreRenderGuiOverlayEvent;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.visuals.ArmorOverlay;
import com.zergatul.cheatutils.modules.visuals.BetterStatusEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract int getVisibleVehicleHeartRows(int p_93013_);

    @Shadow
    protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Shadow
    protected abstract int getVehicleMaxHearts(LivingEntity p_93023_);

    @Inject(at = @At("HEAD"), method = "renderEffects", cancellable = true)
    private void onRenderEffects(GuiGraphics graphics, DeltaTracker delta, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().statusEffectsConfig.enabled) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void onBeforeRender(GuiGraphics graphics, DeltaTracker delta, CallbackInfo info) {
        if (RenderWorldLastEvent.last != null) {
            Events.PreRenderGui.trigger(new RenderGuiEvent(graphics, RenderWorldLastEvent.last));
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void onAfterRender(GuiGraphics graphics, DeltaTracker delta, CallbackInfo info) {
        if (RenderWorldLastEvent.last != null) {
            Events.PostRenderGui.trigger(new RenderGuiEvent(graphics, RenderWorldLastEvent.last));
        }
    }

    @Inject(at = @At("TAIL"), method = "renderPlayerHealth")
    private void onRenderPlayerHealth(GuiGraphics graphics, CallbackInfo info) {
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }

        if (ConfigStore.instance.getConfig().armorOverlayConfig.enabled) {
            int j1 = graphics.guiHeight() - 39;
            int j2 = j1 - 10;

            LivingEntity livingentity = this.getPlayerVehicleWithHealth();
            int l2 = this.getVehicleMaxHearts(livingentity);
            if (l2 == 0) {
                j2 -= 10;
            }

            int k3 = this.getVisibleVehicleHeartRows(l2) - 1;
            j2 -= k3 * 10;

            int i3 = player.getMaxAirSupply();
            int j3 = Math.min(player.getAirSupply(), i3);
            if (player.isEyeInFluid(FluidTags.WATER) || j3 < i3) {
                j2 -= 10;
            }

            ArmorOverlay.instance.render(graphics, player, graphics.guiWidth() / 2 + 28, j2 - 6);
        }

        if (ConfigStore.instance.getConfig().statusEffectsConfig.enabled) {
            BetterStatusEffects.instance.render(graphics, player, graphics.guiHeight() - 50 - 2 * this.minecraft.font.lineHeight - 25);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderTabList", cancellable = true)
    private void onRenderTabList(GuiGraphics graphics, DeltaTracker delta, CallbackInfo info) {
        if (Events.PreRenderGuiOverlay.trigger(new PreRenderGuiOverlayEvent(PreRenderGuiOverlayEvent.GuiOverlayType.PLAYER_LIST))) {
            info.cancel();
        }
    }
}