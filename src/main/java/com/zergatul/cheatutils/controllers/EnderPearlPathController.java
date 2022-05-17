package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EnderPearlPathController {

    public static final EnderPearlPathController instance = new EnderPearlPathController();

    // TODO: highlight block?

    private EnderPearlPathController() {

    }

    public boolean shouldDrawPath() {
        if (!ConfigStore.instance.getConfig().enderPearlPathConfig.enabled) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }

        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return itemStack.getItem() == Items.ENDER_PEARL;
    }
}
