package com.zergatul.cheatutils.modules.automation;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BreachSwapConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.InputConstantsKeyAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.wrappers.AttackRange;
import net.minecraft.client.Minecraft;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BreachSwap implements Module {
    public static final BreachSwap instance = new BreachSwap();
    private final Minecraft mc = Minecraft.getInstance();
    private final float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
    public boolean attacked;


    private final Map<String, InputConstants.Key> keyMap = new HashMap<>();

    private boolean isKeyDown(String key) {
        if (!mc.isWindowActive()) {
            return false;
        }

        InputConstants.Key inputKey = keyMap.get(key);
        if (inputKey == null) {
            return false;
        }
        if (inputKey.getType() == InputConstants.Type.KEYSYM) {
            return InputConstants.isKeyDown(mc.getWindow(), inputKey.getValue());
        }
        if (inputKey.getType() == InputConstants.Type.MOUSE) {
            return org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().handle(), inputKey.getValue()) == 1;
        }

        return false;
    }


    private BreachSwap() {

        for (InputConstants.Key key : InputConstantsKeyAccessor.getNameMap().values()) {
            StringBuilder sb = new StringBuilder();
            key.getDisplayName().visit(cc -> {
                sb.append(cc);
                return Optional.empty();
            });
            keyMap.put(sb.toString(), key);
        }

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

        if (isKeyDown(config.triggerKey)) {
            attacked = false;
            return;
        }
        if (mc.hitResult == null) {
            return;
        }

        if (mc.hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        if (attacked && !config.autoHit) {//Only check for cooldown if auto hit is enabled
            return;
        } else if (mc.player.getAttackStrengthScale((float) 0) != 1) {
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

            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getItem(i);
                if (item.getTags().anyMatch(tag -> tag == ItemTags.SWORDS)) sword = i;
                else if (item.getTags().anyMatch(tag -> tag == ItemTags.AXES)) axe = i;
                else if (item.getEnchantments().keySet().stream().anyMatch(enchantment -> enchantment.value().effects().keySet().contains(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS)))
                    mace = i;
            }

            if (mace == -1) {
                return;
            }

            if (axe == -1 && sword == -1) {
                return;
            }

            if (config.useAxe) {//Prefer Axe to sword
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


            if (config.breakShield) {
                boolean isUsingShield = false;
                if (entity instanceof LivingEntity living) {
                    if(living.isBlocking()) {
                        Vec3 targetLookAngle = living.getViewVector(partialTicks);
                        Vec3 playerAngle = mc.player.getEyePosition(partialTicks).subtract(living.getEyePosition(partialTicks)).normalize();
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

            attacked = true;
            inventory.setSelectedSlot(mace);
            mc.gameMode.attack(mc.player, entity);
            mc.player.swing(InteractionHand.MAIN_HAND);
            inventory.setSelectedSlot(weapon);
        }
    }
}