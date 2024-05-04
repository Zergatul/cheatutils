package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.AutoEatConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.helpers.MixinMultiPlayerGameModeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public class AutoEat {

    public static final AutoEat instance = new AutoEat();

    private final Minecraft mc = Minecraft.getInstance();
    private State state;

    private AutoEat() {
        Events.ClientTickStart.add(this::onClickTickStart);
        state = State.NONE;
    }

    private void onClickTickStart() {
        if (mc.player == null || mc.level == null) {
            return;
        }

        AutoEatConfig config = ConfigStore.instance.getConfig().autoEatConfig;
        if (config.enabled && (!config.isHungerLimitEnabled || mc.player.getFoodData().getFoodLevel() <= config.hungerLimit)) {
            ItemStack itemStack = mc.player.getInventory().offhand.get(0);
            FoodProperties food = itemStack.get(DataComponents.FOOD);
            if (food != null && mc.player.getFoodData().needsFood()) {
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
            InteractionResult result = mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
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