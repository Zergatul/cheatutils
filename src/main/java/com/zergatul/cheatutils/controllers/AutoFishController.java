package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.JTextComponent;
import java.util.List;

public class AutoFishController {

    public static final AutoFishController instance = new AutoFishController();

    private static final long REHOOK_DELAY = 500000000;
    private static final long NO_PULL_DELAY = 2000000000;

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(AutoFishController.class);
    private FishingHook bobber;
    private long lastPullIn;
    private long lastRodUse;

    private AutoFishController() {
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        if (ConfigStore.instance.autoFish) {
            if (event.phase == TickEvent.Phase.END) {
                if (mc.player == null) {
                    return;
                }

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
                            var key = Minecraft.getInstance().options.keyUse.getKey();
                            KeyMapping.click(key);
                            lastPullIn = 0;
                            lastRodUse = System.nanoTime();
                        } else {
                            lastPullIn = 0;
                        }
                    }
                }
            }
        } else {
            bobber = null;
            lastPullIn = 0;
        }

    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (bobber != null && ConfigStore.instance.autoFish) {
            if (args.packet instanceof ClientboundMoveEntityPacket) {
                var packet = (ClientboundMoveEntityPacket) args.packet;
                if (packet.getEntity(mc.level) == bobber) {
                    long time = System.nanoTime();
                    double deltaY = ClientboundMoveEntityPacket.packetToEntity(packet.getYa());
                    //logger.info("delta y = {}", deltaY);
                    if (Math.abs(deltaY) > 0.35 && time - lastRodUse > NO_PULL_DELAY) {
                        var key = Minecraft.getInstance().options.keyUse.getKey();
                        KeyMapping.click(key);
                        lastPullIn = System.nanoTime();
                    }
                }
            }
        }
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (ConfigStore.instance.autoFish) {
            if (args.packet instanceof ServerboundUseItemPacket) {
                var packet = (ServerboundUseItemPacket) args.packet;
                var itemStack = mc.player.getItemInHand(packet.getHand());
                if (itemStack.getItem() == Items.FISHING_ROD) {
                    lastRodUse = System.nanoTime();
                }
            }
        }
    }

}