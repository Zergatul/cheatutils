package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BeforeAttackEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.core.component.DataComponents.ATTACK_RANGE;

public class SpearRange implements Module {
    public static final SpearRange instance = new SpearRange();
    private final Minecraft mc = Minecraft.getInstance();
    private Inventory inventory;
    int prevSelectedSlot = -1;

    private SpearRange() {
        Events.BeforeAttack.add(this::onBeforeAttack);
        Events.AfterAttack.add(this::onAfterAttack);
    }

    public void onBeforeAttack(BeforeAttackEvent event) {
        if (!ConfigStore.instance.getConfig().spearRangeConfig.enabled) return;
        prevSelectedSlot = -1;
        if (mc.player == null) return;
        if (mc.player.isSpectator()) return;
        inventory = mc.player.getInventory();

        int spear = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.getComponents().stream().anyMatch(component -> component.type() == ATTACK_RANGE)) {
                spear = i;
                break;
            }
        }
        if (spear == -1) return;
        if (spear == prevSelectedSlot) return;

        prevSelectedSlot = inventory.getSelectedSlot();
        inventory.setSelectedSlot(spear);
    }

    private void onAfterAttack() {
        if (!ConfigStore.instance.getConfig().spearRangeConfig.enabled) return;
        if (prevSelectedSlot == -1) return;
        inventory.setSelectedSlot(prevSelectedSlot);
    }
}
