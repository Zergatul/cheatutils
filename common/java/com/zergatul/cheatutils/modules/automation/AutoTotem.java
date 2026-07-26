package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.AutoTotemConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AutoTotem implements Module {

    public static final AutoTotem instance = new AutoTotem();

    private final Minecraft mc = Minecraft.getInstance();

    private AutoTotem() {
        Events.InGameTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player == null) {
            return;
        }

        AutoTotemConfig config = ConfigStore.instance.getConfig().autoTotemConfig;
        if (!config.enabled) {
            return;
        }

        if (config.skipIfUsingItem && mc.player.isUsingItem()) {
            return;
        }

        ItemStack offhand = mc.player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (offhand.isEmpty()) {
            Inventory inventory = mc.player.getInventory();
            int totemSlot = -1;
            for (int i = 0; i < 36; i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack.has(DataComponents.DEATH_PROTECTION)) {
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