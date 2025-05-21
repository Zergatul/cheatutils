package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerReleaseUsingItemEvent;
import com.zergatul.cheatutils.configs.AutoEatConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
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
        Events.PlayerReleaseUsingItem.add(this::onPlayerReleaseUsingItem);
        state = State.NONE;
    }

    private void onClickTickStart() {
        if (mc.player == null) {
            return;
        }

        AutoEatConfig config = ConfigStore.instance.getConfig().autoEatConfig;
        if (shouldStartEating(config)) {
            ItemStack itemStack = mc.player.getOffhandItem();
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

    private boolean shouldStartEating(AutoEatConfig config) {
        assert mc.player != null;

        if (!config.enabled) {
            return false;
        }
        if (state != State.EATING && mc.player.isUsingItem()) {
            return false;
        }
        return !config.isHungerLimitEnabled || mc.player.getFoodData().getFoodLevel() <= config.hungerLimit;
    }

    private void onPlayerReleaseUsingItem(PlayerReleaseUsingItemEvent event) {
        if (state == State.EATING) {
            event.cancel();
        }
    }

    private void startEating() {
        assert mc.player != null;
        assert mc.gameMode != null;

        if (state == State.NONE || mc.player.getUseItem().isEmpty()) {
            InteractionResult result = mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
            if (result.consumesAction()) {
                mc.gameRenderer.itemInHandRenderer.itemUsed(InteractionHand.OFF_HAND);
                state = State.EATING;
            }
        }
    }

    private void stopEating() {
        assert mc.player != null;
        assert mc.gameMode != null;

        if (state == State.EATING) {
            mc.gameMode.releaseUsingItem(mc.player);
            state = State.NONE;
        }
    }

    private enum State {
        NONE,
        EATING
    }
}