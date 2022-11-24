package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.KeyUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

public class AutoFishController {

    public static final AutoFishController instance = new AutoFishController();

    private static final long REHOOK_DELAY = 500000000;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private FishingBobberEntity bobber;
    private long lastPullIn;

    private AutoFishController() {
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player == null) {
            return;
        }

        if (ConfigStore.instance.getConfig().autoFishConfig.enabled) {
            Box box = new Box(
                    mc.player.getX() - 100,
                    mc.player.getY() - 100,
                    mc.player.getZ() - 100,
                    mc.player.getX() + 100,
                    mc.player.getY() + 100,
                    mc.player.getZ() + 100);
            var bobbers = mc.player.world.getEntitiesByClass(FishingBobberEntity.class, box, e -> true);
            bobber = bobbers.stream().filter(b -> b.getPlayerOwner() == mc.player).findFirst().orElse(null);

            if (bobber == null && lastPullIn != 0) {
                if (System.nanoTime() - lastPullIn > REHOOK_DELAY) {
                    ItemStack stack = mc.player.getMainHandStack();
                    if (stack.getItem() == Items.FISHING_ROD) {
                        KeyUtils.click(mc.options.useKey);
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
        if (args.packet instanceof PlaySoundS2CPacket soundPacket) {
            if (soundPacket.getSound() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
                if (bobber.squaredDistanceTo(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()) < 1) {
                    KeyUtils.click(mc.options.useKey);
                    lastPullIn = System.nanoTime();
                }
            }
        }
    }
}