package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AutoTotemController {

    public static final AutoTotemController instance = new AutoTotemController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(AutoTotemController.class);

    private AutoTotemController() {
        //NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (ConfigStore.instance.getConfig().autoTotemConfig.enabled) {
                if (mc.player == null) {
                    return;
                }

                ItemStack offhand = mc.player.getItemBySlot(EquipmentSlot.OFFHAND);
                if (offhand.isEmpty()) {
                    Inventory inventory = mc.player.getInventory();
                    int totemSlot = -1;
                    for (int i = 0; i < 36; i++) {
                        ItemStack itemStack = inventory.getItem(i);
                        if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
                            totemSlot = i;
                            break;
                        }
                    }

                    if (totemSlot >= 0) {
                        int serverSlot = totemSlot < 9 ? totemSlot + 36 : totemSlot;
                        Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
                        int2objectmap.put(serverSlot, new ItemStack(Items.AIR, 1));
                        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                                0, // containerId
                                mc.player.inventoryMenu.getStateId(),
                                serverSlot,
                                0, // buttonNum
                                ClickType.PICKUP,
                                inventory.getItem(serverSlot),
                                int2objectmap
                        ));
                        inventory.setItem(totemSlot, int2objectmap.get(serverSlot));

                        int2objectmap = new Int2ObjectOpenHashMap<>();
                        int2objectmap.put(45, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
                        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                                0, // containerId
                                mc.player.inventoryMenu.getStateId(),
                                45,
                                0, // buttonNum
                                ClickType.PICKUP,
                                new ItemStack(Items.AIR, 1),
                                int2objectmap
                        ));
                        mc.player.setItemSlot(EquipmentSlot.OFFHAND, int2objectmap.get(45));

                        if (!(mc.screen instanceof InventoryScreen)) {
                            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClosePacket(0));
                        }
                    }
                }
            }
        }
    }

    /*private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket) {
            return;
        }
        if (args.packet instanceof ServerboundPongPacket) {
            return;
        }
        if (args.packet instanceof ServerboundContainerClosePacket packet) {
            logger.info("Close id={}", packet.getContainerId());
            return;
        }
        if (args.packet instanceof ServerboundContainerClickPacket packet) {
            String changedSlots = "";
            for (var key: packet.getChangedSlots().keySet()) {
                changedSlots += "[" + key + "]=[" + packet.getChangedSlots().get(key)+ "]";
            }
            logger.info("Click id={} btn={} clicktype={} state={} stack=[{}] slot={} changedSlots.count={}",
                    packet.getContainerId(), packet.getButtonNum(), packet.getClickType(), packet.getStateId(),
                    packet.getCarriedItem(), packet.getSlotNum(), changedSlots);
            return;
        }
        logger.info(args.packet.getClass().getName());
    }*/
}
