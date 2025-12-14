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
import com.zergatul.cheatutils.scripting.Root;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

public class BreachSwap implements Module {

    public static final BreachSwap instance = new BreachSwap();
    

    private final Minecraft mc = Minecraft.getInstance();
    public boolean attacked;

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

        if(!Root.input.isKeyDown(config.triggerKey)) {
            attacked = false;
            return;
        }
        if (mc.hitResult == null) {
            return;
        }

        if (mc.hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        if(attacked && !config.autoHit){//Only check for cooldown if auto hit is enabled
            return;
        }

        else if(mc.player.getAttackStrengthScale((float) 0) != 1) {
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
            Inventory inventory = mc.player.getInventory();

            for(int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if(item.getTags().anyMatch(tag -> tag == ItemTags.SWORDS))sword = i; 
                else if(item.getTags().anyMatch(tag -> tag == ItemTags.AXES))axe = i;
                else if(item.getEnchantments().keySet().stream().anyMatch(enchantment -> enchantment.value().effects().keySet().contains(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS)))mace = i;
            }

            if(mace == -1) {
                return;
            }

            if(axe == -1 && sword == -1) {
                return;
            }

            if(config.useAxe) {//Prefer Axe over sword
                if(axe != -1) {
                    weapon = axe;
                }
                else {
                    weapon = sword;
                }
            }
            else {
                if(sword != -1) {
                    weapon = sword;
                }
                else {
                    weapon = axe;
                }
            }

            if(config.breakShield) {
                if(Root.game.entities.isUsingItemWithOffHand(entity.getId()) && Root.game.entities.getEquippedOffHandItem(entity.getId()).getItem().getId().equals("minecraft:shield")){

                    if(axe != -1) {
                        inventory.setSelectedSlot(axe);
                        mc.gameMode.attack(mc.player, entity);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    }

                }

            }

            attacked = true;
            inventory.setSelectedSlot(mace);
            mc.gameMode.attack(mc.player, entity);
            mc.player.swing(InteractionHand.MAIN_HAND);
            inventory.setSelectedSlot(weapon);
        }
    }
}