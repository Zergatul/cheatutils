package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BeforeAttackEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.MultiPlayerGameModeExtension;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

import static net.minecraft.core.component.DataComponents.ATTACK_RANGE;

public class SpearRange implements Module {
    public static final SpearRange instance = new SpearRange();
    private final Minecraft mc = Minecraft.getInstance();
    int prevSelectedSlot = -1;

    /**
     *
     * @param inventory player inventory
     * @return returns position of spear in the hotbar. If no spear is present, returns -1
     */
    public final int spearPos(Inventory inventory) {
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
        Events.BeforeAttack.add(this::onBeforeAttack, 0);
    }

    public void onBeforeAttack(BeforeAttackEvent event) {
        if (!ConfigStore.instance.getConfig().spearRangeConfig.enabled) return;
        prevSelectedSlot = -1;
        if (mc.player == null) return;
        if (mc.player.isSpectator()) return;
        Inventory inventory = mc.player.getInventory();


        //If already in range normally, do nothing
        assert mc.hitResult != null;
        final double reach = 3;
        if (reach * reach > mc.player.getEyePosition().distanceToSqr(mc.hitResult.getLocation())) return;

        int spear = spearPos(inventory);

        if (spear == -1) return;
        if (spear == prevSelectedSlot) return;

        prevSelectedSlot = inventory.getSelectedSlot();
        inventory.setSelectedSlot(spear);

        assert mc.gameMode != null;
        ((MultiPlayerGameModeExtension) mc.gameMode).attackClone_CU(mc.player, ((EntityHitResult) mc.hitResult).getEntity());
        mc.player.swing(InteractionHand.MAIN_HAND);

        inventory.setSelectedSlot(prevSelectedSlot);

        event.cancel();
    }
}
