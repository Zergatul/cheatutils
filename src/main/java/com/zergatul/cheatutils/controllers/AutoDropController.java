package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoDropController {

    public static final AutoDropController instance = new AutoDropController();

    public final List<Item> items = new ArrayList<>();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean checkNextTick;
    private final List<InventorySlot> stacksToDrop = new ArrayList<>();

    private AutoDropController() {
        //NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);

        items.add(Items.LILY_PAD);
        items.add(Items.BOWL);
        items.add(Items.LEATHER_BOOTS);
        items.add(Items.ROTTEN_FLESH);
        items.add(Items.GLASS_BOTTLE);
        items.add(Items.BONE);
        items.add(Items.INK_SAC);
        items.add(Items.TRIPWIRE_HOOK);
        items.add(Items.SADDLE);
        items.add(Items.GLASS_BOTTLE);
    }

    /*@SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && checkNextTick) {

            checkNextTick = false;

            stacksToDrop.clear();
            for (int i = 0; i < mc.player.getInventory().items.size(); i++) {
                ItemStack stack = mc.player.getInventory().items.get(i);
                Item item = stack.getItem();
                synchronized (items) {
                    for (Item toDrop : items) {
                        if (item == toDrop) {
                            stacksToDrop.add(new InventorySlot(stack, i));
                            mc.player.getInventory().items.set(i, new ItemStack(Items.AIR, 1));
                            break;
                        }
                    }
                }
            }

            for (InventorySlot slot : stacksToDrop) {

                short uid = mc.player.containerMenu.backup(mc.player.inventory);
                CClickWindowPacket packet = new CClickWindowPacket(
                    0,
                    slot.index < 9 ? 36 + slot.index : slot.index,
                    0,
                    ClickType.PICKUP,
                    slot.stack,
                    uid);
                NetworkPacketsController.instance.sendPacket(packet);


                uid = mc.player.containerMenu.backup(mc.player.inventory);
                packet = new CClickWindowPacket(
                        0,
                        -999,
                        0,
                        ClickType.PICKUP,
                        new ItemStack(Items.AIR, 1),
                        uid);
                NetworkPacketsController.instance.sendPacket(packet);

            }
        }
    }*/

    /*private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (args.packet instanceof SCollectItemPacket) {
            SCollectItemPacket packet = (SCollectItemPacket)args.packet;
            if (packet.getPlayerId() == mc.player.getId()) {
                checkNextTick = true;
            }
        }
    }*/

    private static class InventorySlot {
        public ItemStack stack;
        public int index;

        public InventorySlot(ItemStack stack, int index) {
            this.stack = stack;
            this.index = index;
        }
    }

}
