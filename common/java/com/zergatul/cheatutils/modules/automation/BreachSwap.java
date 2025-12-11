package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BreachSwapConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.wrappers.AttackRange;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import net.minecraft.world.entity.player.Inventory;
import com.zergatul.cheatutils.scripting.types.ItemStackWrapper;

public class BreachSwap implements Module {

    public static final BreachSwap instance = new BreachSwap();

    private final Minecraft mc = Minecraft.getInstance();

    private BreachSwap() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player == null) {
            return;
        }

        BreachSwapConfig config = ConfigStore.instance.getConfig().breachSwapConfig;
        if (!config.enabled) {
            return;
        }

        if (!mc.options.keyAttack.isDown()) {
            return;
        }

        if (mc.hitResult == null) {
            return;
        }
        

        if (mc.hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        if (mc.player.getAttackStrengthScale((float) 0) != 1) {
            return;
        }

        

        Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
        if (AttackRange.canHit(entity)) {
            //Find position of axe, mace and mace
            //Should only run when inventory is updated ideally
            int axe = -1;
            int sword = -1;
            int mace = -1;
            int weapon;
            Inventory invent = mc.player.getInventory();
            for(int i = 0; i < 9; i++){
                ItemStackWrapper item = new ItemStackWrapper(invent.getItem(i));
                if(item.hasEnchantment("breach"))mace = i;
                String itemID = item.getItem().getId();
                if(itemID.endsWith("axe"))axe = i;
                else if(itemID.endsWith("sword"))sword = i;
            }
            if(mace == -1){
                return;
            }

            if(axe == -1 && sword == -1){
                return;
            }

            if(config.useAxe){//Prefer Axe over sword
                if(axe != -1){
                    weapon = axe;
                }
                else {
                    weapon = sword;
                }
            }
            else {
                if(sword != -1){
                    weapon = sword;
                }
                else {
                    weapon = axe;
                }
            }

            invent.setSelectedSlot(mace);
            mc.gameMode.attack(mc.player, entity);
            mc.player.swing(InteractionHand.MAIN_HAND);
            invent.setSelectedSlot(weapon);
        }
    }
}