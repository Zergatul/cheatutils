package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import static net.minecraft.core.component.DataComponents.ATTACK_RANGE;

public class SpearRange implements Module {
    public static final SpearRange instance = new SpearRange();
    private final Minecraft mc = Minecraft.getInstance();
    private int prevSelectedSlot = -1;

    /**
     *
     * @param inventory player inventory
     * @return returns position of spear in the hotbar. If no spear is present, returns -1
     */
    private int spearPos(Inventory inventory) {
        int spear = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.getComponents().stream().anyMatch(component -> component.type() == ATTACK_RANGE)) {
                spear = i;
                break;
            }
        }
        return spear;
    }


    private SpearRange() {
        Events.BeforeStartAttack.add(this::onBeforeStartAttack, 0);
        Events.AfterStartAttack.add(this::onAfterStartAttack, 0);
    }

    public void onBeforeStartAttack() {
        if (!ConfigStore.instance.getConfig().spearRangeConfig.enabled) return;
        prevSelectedSlot = -1;
        if (mc.player == null) return;
        if (mc.player.isSpectator()) return;
        if (mc.hitResult.getType() == HitResult.Type.ENTITY) return;

        Inventory inventory = mc.player.getInventory();
        int spear = spearPos(inventory);
        if (spear == -1) return;
        if (spear == inventory.getSelectedSlot()) return;

        prevSelectedSlot = inventory.getSelectedSlot();
        inventory.setSelectedSlot(spear);
    }

    public void onAfterStartAttack() {
        if (mc.player == null) return;
        if (!ConfigStore.instance.getConfig().spearRangeConfig.enabled) return;
        if (prevSelectedSlot == -1) return;
        mc.player.getInventory().setSelectedSlot(prevSelectedSlot);
        prevSelectedSlot = -1;
    }
}
