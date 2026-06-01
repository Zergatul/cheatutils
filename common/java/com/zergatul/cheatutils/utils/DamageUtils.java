package com.zergatul.cheatutils.utils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DamageUtils {

    private static final float END_CRYSTAL_EXPLOSION_RADIUS = 6.0F;

    public static float calculateEndCrystalDamage(ClientLevel level, EndCrystal crystal, LivingEntity entity) {
        return calculateEndCrystalDamage(level, crystal.position(), entity);
    }

    public static float calculateEndCrystalDamage(ClientLevel level, Vec3 crystalPosition, LivingEntity entity) {
        float doubleRadius = END_CRYSTAL_EXPLOSION_RADIUS * 2.0F;
        double distanceModifier = Math.sqrt(entity.distanceToSqr(crystalPosition)) / doubleRadius;
        if (distanceModifier > 1.0) {
            return 0;
        }

        AABB bb = entity.getBoundingBox();
        float exposure = getSeenPercent(level, crystalPosition, bb, entity);
        double pow = (1.0 - distanceModifier) * exposure;
        float damage = (float) ((pow * pow + pow) / 2.0 * 7.0 * doubleRadius + 1.0);
        return withReductions(level, entity, damage, level.damageSources().explosion(null));
    }

    public static float calculateEndCrystalDamage(ClientLevel level, EndCrystal crystal, LivingEntity entity, Vec3 interpolatedPosition) {
        Vec3 explosionCenter = crystal.position();
        float doubleRadius = END_CRYSTAL_EXPLOSION_RADIUS * 2.0F;
        double distanceModifier = interpolatedPosition.distanceTo(explosionCenter) / doubleRadius;
        if (distanceModifier > 1.0) {
            return 0;
        }

        AABB bb = entity.getBoundingBox().move(interpolatedPosition.subtract(entity.position()));
        float exposure = getSeenPercent(level, explosionCenter, bb, entity);
        double pow = (1.0 - distanceModifier) * exposure;
        float damage = (float) ((pow * pow + pow) / 2.0 * 7.0 * doubleRadius + 1.0);
        return withReductions(level, entity, damage, level.damageSources().explosion(null));
    }

    private static float withReductions(ClientLevel level, LivingEntity entity, float damage, DamageSource source) {
        if (source.scalesWithDifficulty() && entity instanceof Player) {
            switch (level.getDifficulty()) {
                case PEACEFUL -> { return 0; }
                case EASY -> damage = Math.min(damage / 2 + 1, damage);
                case HARD -> damage *= 1.5f;
            }
        }

        // TODO: what if server has plugin that hides enchantments? add option to "assume" some enchantments on all players?
        damage = CombatRules.getDamageAfterAbsorb(entity, damage, source, entity.getArmorValue(), (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));

        if (!source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            // for non-LocalPlayer entities we will not have effects
            MobEffectInstance resistance = entity.getEffect(MobEffects.RESISTANCE);
            if (resistance != null && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                int lvl = resistance.getAmplifier() + 1;
                damage = Math.max(0, damage * (1 - (lvl * 0.2f)));
            }

            damage = withProtectionEnchantments(entity, damage, source);
        }

        return Math.max(0, damage);
    }

    private static float withProtectionEnchantments(LivingEntity entity, float damage, DamageSource source) {
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

    private static float getSeenPercent(ClientLevel level, Vec3 center, AABB bb, LivingEntity entity) {
        double xs = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
        double ys = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
        double zs = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
        double xOffset = (1.0 - Math.floor(1.0 / xs) * xs) / 2.0;
        double zOffset = (1.0 - Math.floor(1.0 / zs) * zs) / 2.0;
        if (xs < 0.0 || ys < 0.0 || zs < 0.0) {
            return 0.0F;
        }

        int hits = 0;
        int count = 0;
        for (double xx = 0.0; xx <= 1.0; xx += xs) {
            for (double yy = 0.0; yy <= 1.0; yy += ys) {
                for (double zz = 0.0; zz <= 1.0; zz += zs) {
                    double x = Mth.lerp(xx, bb.minX, bb.maxX);
                    double y = Mth.lerp(yy, bb.minY, bb.maxY);
                    double z = Mth.lerp(zz, bb.minZ, bb.maxZ);
                    Vec3 from = new Vec3(x + xOffset, y, z + zOffset);
                    ClipContext context = new ClipContext(from, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
                    if (level.clip(context).getType() == HitResult.Type.MISS) {
                        hits++;
                    }

                    count++;
                }
            }
        }

        return (float) hits / count;
    }
}