package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BeforeAttackEvent;
import com.zergatul.cheatutils.configs.BreachSwapConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.phys.Vec3;

public class BreachSwap implements Module {
    public static final BreachSwap instance = new BreachSwap();
    private final Minecraft mc = Minecraft.getInstance();

    private BreachSwap() {
        Events.BeforeAttack.add(this::onBeforeAttack);
        handling = false;
    }


    public boolean handling;

    public void onBeforeAttack() {
        if (handling) {
            return;
        }
        BreachSwapConfig config = ConfigStore.instance.getConfig().breachSwapConfig;
        if (config.enabled) {
            run(config.useAxe, config.breakShield);
        }
    }

    public boolean run(boolean useAxe, boolean breakShield) {
        handling = true;
        if (mc.player == null) {
            handling = false;
            return false;
        }

        if (mc.hitResult == null) {
            handling = false;
            return false;
        }

        if (mc.hitResult.getType() != HitResult.Type.ENTITY) {
            handling = false;
            return false;
        }

        Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
        double reach = mc.player.entityInteractionRange();
        if (reach * reach < mc.player.getEyePosition().distanceToSqr(mc.hitResult.getLocation())) {
            handling = false;
            return false;
        }
        //Find position of axe, sword and mace
        //Should only run when inventory is updated ideally
        int axe = -1;
        int sword = -1;
        int mace = -1;
        int weapon;
        Inventory inventory = mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.getTags().anyMatch(tag -> tag == ItemTags.SWORDS)) sword = i;
            else if (item.getTags().anyMatch(tag -> tag == ItemTags.AXES)) axe = i;
            else if (item.getEnchantments().keySet().stream().anyMatch(enchantment ->
                    enchantment.value().effects().keySet().contains(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS)))
                mace = i;
        }

        if (mace == -1) {
            handling = false;
            return false;
        }

        if (axe == -1 && sword == -1) {
            handling = false;
            return false;
        }

        if (useAxe) {//Prefer Axe to sword
            if (axe != -1) {
                weapon = axe;
            } else {
                weapon = sword;
            }
        } else {//Prefer sword to axe
            if (sword != -1) {
                weapon = sword;
            } else {
                weapon = axe;
            }
        }

        if (breakShield) {
            boolean isUsingShield = false;
            if (entity instanceof LivingEntity living) {
                if (living.isBlocking()) {
                    Vec3 targetLookAngle = living.getLookAngle();
                    Vec3 playerAngle = mc.player.getEyePosition().subtract(living.getEyePosition()).normalize();
                    double dotProduct = targetLookAngle.dot(playerAngle);
                    isUsingShield = dotProduct < 0;
                }
            }


            if (isUsingShield) {
                if (axe != -1) {
                    inventory.setSelectedSlot(axe);
                    mc.gameMode.attack(mc.player, entity);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }

        inventory.setSelectedSlot(mace);
        mc.gameMode.attack(mc.player, entity);
        mc.player.swing(InteractionHand.MAIN_HAND);
        inventory.setSelectedSlot(weapon);

        handling = false;
        return true;

    }
}