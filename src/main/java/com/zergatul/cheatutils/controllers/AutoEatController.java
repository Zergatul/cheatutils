package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.helpers.MixinMultiPlayerGameModeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoEatController {

    public static final AutoEatController instance = new AutoEatController();

    private final Minecraft mc = Minecraft.getInstance();
    private State state;

    private AutoEatController() {
        state = State.NONE;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        var config = ConfigStore.instance.getConfig().autoEatConfig;
        if (config.enabled) {
            ItemStack itemStack = mc.player.getInventory().offhand.get(0);
            if (itemStack.isEdible() && mc.player.getFoodData().needsFood()) {
                startEating();
            } else {
                stopEating();
            }
        } else {
            stopEating();
        }
    }

    private void startEating() {
        if (state == State.NONE || mc.player.getUseItem().isEmpty()) {
            InteractionResult result = mc.gameMode.useItem(mc.player, mc.level, InteractionHand.OFF_HAND);
            if (result.consumesAction()) {
                mc.gameRenderer.itemInHandRenderer.itemUsed(InteractionHand.OFF_HAND);
                MixinMultiPlayerGameModeHelper.disableReleaseUsingItem = true;
                state = State.EATING;
            }
        }
    }

    private void stopEating() {
        if (state == State.EATING) {
            mc.gameMode.releaseUsingItem(mc.player);
            MixinMultiPlayerGameModeHelper.disableReleaseUsingItem = false;
            state = State.NONE;
        }
    }

    private enum State {
        NONE,
        EATING
    }
}