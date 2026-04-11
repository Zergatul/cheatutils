package com.zergatul.cheatutils.utils;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class HotbarUtils {

    public static boolean hasItem(LocalPlayer player, Item item) {
        if (player.getMainHandItem().is(item)) {
            return true;
        } else if (player.getOffhandItem().is(item)) {
            return true;
        } else {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).is(item)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static Optional<InteractionHand> selectItem(LocalPlayer player, Item item) {
        if (player.getMainHandItem().is(item)) {
            return Optional.of(InteractionHand.MAIN_HAND);
        } else if (player.getOffhandItem().is(item)) {
            return Optional.of(InteractionHand.OFF_HAND);
        } else {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).is(item)) {
                    inventory.setSelectedSlot(i);
                    return Optional.of(InteractionHand.MAIN_HAND);
                }
            }

            return Optional.empty();
        }
    }

    public static Optional<HotbarSlot> findItem(LocalPlayer player, Item item) {
        if (player.getMainHandItem().is(item)) {
            return Optional.of(HotbarSlot.createMainHand(player.getInventory().getSelectedSlot()));
        } else if (player.getOffhandItem().is(item)) {
            return Optional.of(HotbarSlot.createOffHand());
        } else {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).is(item)) {
                    return Optional.of(HotbarSlot.createMainHand(i));
                }
            }

            return Optional.empty();
        }
    }
}