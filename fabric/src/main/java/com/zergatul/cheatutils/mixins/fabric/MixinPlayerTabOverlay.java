package com.zergatul.cheatutils.mixins.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PreRenderGuiOverlayEvent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class MixinPlayerTabOverlay {

    @Inject(
            at = @At("HEAD"),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V",
            cancellable = true)
    private void onRender(PoseStack matrices, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo info) {
        if (Events.PreRenderGuiOverlay.trigger(new PreRenderGuiOverlayEvent(PreRenderGuiOverlayEvent.GuiOverlayType.PLAYER_LIST))) {
            info.cancel();
        }
    }
}