package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoTotem implements Module {

    public static final AutoTotem instance = new AutoTotem();

    private final Minecraft mc = Minecraft.getInstance();

    private AutoTotem() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
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
                    InventoryUtils.moveItemStack(new InventorySlot(totemSlot), new InventorySlot(EquipmentSlot.OFFHAND));
                }
            }
        }
    }
}
