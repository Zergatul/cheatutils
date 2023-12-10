package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.configs.AutoDropConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AutoDropApi {

    @ApiVisibility(ApiType.ACTION)
    public void dropItems() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            AutoDropConfig config = ConfigStore.instance.getConfig().autoDropConfig;
            List<InventorySlot> slots = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                ItemStack itemStack = mc.player.getInventory().getItem(i);
                if (!itemStack.isEmpty() && config.items.contains(itemStack.getItem())) {
                    slots.add(new InventorySlot(i));
                }
            }
            InventoryUtils.dropItemStacks(slots);
        }
    }
}