package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BeforeAttackEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.phys.HitResult;

public class BreachSwap implements Module {
    public static final BreachSwap instance = new BreachSwap();
    private final Minecraft mc = Minecraft.getInstance();
    private Inventory inventory;

    private static int prevSelectedSlot = -1;

    private BreachSwap() {
        Events.AttackEventHandler(this::onBeforeAttack, this::onAfterAttack, 2);
    }

    private void onBeforeAttack(BeforeAttackEvent event) {

        if (!ConfigStore.instance.getConfig().breachSwapConfig.enabled) {
            return;
        }
        prevSelectedSlot = -1;
        if (mc.player == null) {
            return;
        }

        if (mc.hitResult == null) {
            return;
        }

        if (mc.hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        int mace = -1;
        inventory = mc.player.getInventory();


        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);

            if (item.getEnchantments().keySet().stream().anyMatch(enchantment ->
                    enchantment.value().effects().keySet().contains(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS)))
                mace = i;
        }

        if (mace == -1) {
            return;
        }
        prevSelectedSlot = inventory.getSelectedSlot();
        inventory.setSelectedSlot(mace);

    }

    private void onAfterAttack() {
        if (!ConfigStore.instance.getConfig().breachSwapConfig.enabled) return;
        if (prevSelectedSlot == -1) return;
        inventory.setSelectedSlot(prevSelectedSlot);
    }


}