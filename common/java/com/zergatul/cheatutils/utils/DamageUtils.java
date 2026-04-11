package com.zergatul.cheatutils.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class DamageUtils {

    public static final float END_CRYSTAL_EXPLOSION_RADIUS = 6.0F;

    private static final int NUM_ARMOR_ITEMS = 4; // from CombatRules

    public static float applyReductions(Difficulty difficulty, LivingEntity entity, float damage, DamageSource source) {
        if (source.scalesWithDifficulty() && entity instanceof Player) {
            switch (difficulty) {
                case PEACEFUL -> { return 0; }
                case EASY -> damage = Math.min(damage / 2 + 1, damage);
                case HARD -> damage *= 1.5f;
            }
        }

        // TODO: what if server has plugin that hides enchantments? add option to "assume" some enchantments on all players?
        damage = getDamageAfterAbsorb(damage, entity.getArmorValue(), (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));

        if (!source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            // for non-LocalPlayer entities we will not have effects
            MobEffectInstance resistance = entity.getEffect(MobEffects.RESISTANCE);
            if (resistance != null && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                int lvl = resistance.getAmplifier() + 1;
                damage = Math.max(0, damage * (1 - (lvl * 0.2f)));
            }

            damage = applyProtectionEnchantments(entity, damage, source);
        }

        return Math.max(0, damage);
    }

    // logic from CombatRules.getDamageAfterAbsorb
    public static float getDamageAfterAbsorb(float damage, float totalArmor, float armorToughness) {
        float toughness = CombatRules.BASE_ARMOR_TOUGHNESS + armorToughness / NUM_ARMOR_ITEMS;
        float realArmor = Mth.clamp(totalArmor - damage / toughness, totalArmor * CombatRules.MIN_ARMOR_RATIO, CombatRules.MAX_ARMOR);
        float armorFraction = realArmor / CombatRules.ARMOR_PROTECTION_DIVIDER;
        float damageMultiplier = 1.0F - armorFraction;
        return damage * damageMultiplier;
    }

    private static float applyProtectionEnchantments(LivingEntity entity, float damage, DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }

        int damageProtection = 0;
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            ItemStack stack = entity.getItemBySlot(slot);
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

            for (Holder<Enchantment> holder : enchantments.keySet()) {
                if (holder.is(Enchantments.PROTECTION)) {
                    damageProtection += enchantments.getLevel(holder);
                }
                if (holder.is(Enchantments.FIRE_PROTECTION) && source.is(DamageTypeTags.IS_FIRE)) {
                    damageProtection += 2 * enchantments.getLevel(holder);
                }
                if (holder.is(Enchantments.BLAST_PROTECTION) && source.is(DamageTypeTags.IS_EXPLOSION)) {
                    damageProtection += 2 * enchantments.getLevel(holder);
                }
                if (holder.is(Enchantments.PROJECTILE_PROTECTION) && source.is(DamageTypeTags.IS_PROJECTILE)) {
                    damageProtection += 2 * enchantments.getLevel(holder);
                }
                if (holder.is(Enchantments.FEATHER_FALLING) && source.is(DamageTypeTags.IS_FALL)) {
                    damageProtection += 3 * enchantments.getLevel(holder);
                }
            }
        }

        return CombatRules.getDamageAfterMagicAbsorb(damage, damageProtection);
    }
}