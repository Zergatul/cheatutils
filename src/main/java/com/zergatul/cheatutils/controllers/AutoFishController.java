package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.KeyUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AutoFishController {

    public static final AutoFishController instance = new AutoFishController();

    private static final long REHOOK_DELAY = 500000000;
    private static final long NO_PULL_DELAY = 2000000000;

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(AutoFishController.class);
    private FishingHook bobber;
    private long lastPullIn;

    private AutoFishController() {
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
        ModApiWrapper.addOnClientTickEnd(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player == null) {
            return;
        }

        if (ConfigStore.instance.getConfig().autoFishConfig.enabled) {
            AABB box = new AABB(
                mc.player.getX() - 100,
                mc.player.getY() - 100,
                mc.player.getZ() - 100,
                mc.player.getX() + 100,
                mc.player.getY() + 100,
                mc.player.getZ() + 100);
            var bobbers = mc.player.clientLevel.getEntitiesOfClass(FishingHook.class, box);
            bobber = bobbers.stream().filter(b -> b.getPlayerOwner() == mc.player).findFirst().orElse(null);

            if (bobber == null && lastPullIn != 0) {
                if (System.nanoTime() - lastPullIn > REHOOK_DELAY) {
                    ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
                    if (stack.getItem() == Items.FISHING_ROD) {
                        KeyUtils.click(mc.options.keyUse);
                    }
                    lastPullIn = 0;
                }
            }
        } else {
            bobber = null;
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (bobber == null) {
            return;
        }
        if (!ConfigStore.instance.getConfig().autoFishConfig.enabled) {
            return;
        }
        if (args.packet instanceof ClientboundSoundPacket soundPacket) {
            if (soundPacket.getSound() == SoundEvents.FISHING_BOBBER_SPLASH) {
                if (bobber.distanceToSqr(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()) < 1) {
                    KeyUtils.click(mc.options.keyUse);
                    lastPullIn = System.nanoTime();
                }
            }
        }
    }
}