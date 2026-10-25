package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AutoTotemConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AutoTotem implements Module {

    public static final AutoTotem INSTANCE = new AutoTotem();

    private final SettingHandle2<AutoTotemConfig> settings = Settings.get(AutoTotemConfig.class);
    private final Minecraft mc = Minecraft.getInstance();

    private AutoTotem() {
        Events.InGameTickEnd.add(this::onClientTickEnd);

        WebApiRegistry.INSTANCE.register("auto-totem", settings, AutoTotemConfig.class);
    }

    public SettingHandle2<AutoTotemConfig> getSettings() {
        return settings;
    }

    private void onClientTickEnd() {
        if (mc.player == null) {
            return;
        }

        AutoTotemConfig config = settings.get();
        if (!config.enabled()) {
            return;
        }

        if (config.skipIfUsingItem() && mc.player.isUsingItem()) {
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