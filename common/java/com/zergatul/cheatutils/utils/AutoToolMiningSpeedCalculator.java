package com.zergatul.cheatutils.utils;

import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoToolMiningSpeedCalculator {

    private final Map<Holder<Attribute>, Double> baseValues = new HashMap<>();
    private final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
    private final boolean submerged;

    public AutoToolMiningSpeedCalculator(Player player) {
        this(player.getAttributes(), getEquipment(player), player.isEyeInFluid(FluidTags.WATER));
    }

    public AutoToolMiningSpeedCalculator(
            AttributeMap attributes,
            Map<EquipmentSlot, ItemStack> equipment,
            boolean submerged
    ) {
        this.submerged = submerged;
        for (Holder<Attribute> attribute : List.of(Attributes.MINING_EFFICIENCY, Attributes.BLOCK_BREAK_SPEED, Attributes.SUBMERGED_MINING_SPEED)) {
            AttributeInstance original = attributes.getInstance(attribute);
            // Live modifiers may describe a previously held tool. Only use player base values;
            // equipment bonuses are rebuilt below, and arbitrary server modifiers are omitted.
            baseValues.put(attribute, original != null ? original.getBaseValue() : attribute.value().getDefaultValue());
        }

        equipment.forEach((slot, item) -> {
            if (slot != EquipmentSlot.MAINHAND) {
                this.equipment.put(slot, item.copy());
            }
        });
    }

    public double getMiningSpeed(BlockState state, ItemStack item) {
        Map<Holder<Attribute>, AttributeInstance> attributes = new HashMap<>();
        baseValues.forEach((attribute, value) -> {
            AttributeInstance instance = new AttributeInstance(attribute, ignored -> {});
            instance.setBaseValue(value);
            attributes.put(attribute, instance);
        });

        // Model a fresh application of all equipment in vanilla enum order:
        // main hand, offhand, feet, legs, chest, head, body, saddle. Later IDs win.
        // This deliberately ignores the history of vanilla's incremental equipment updates.
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            applyModifiers(attributes, slot, slot == EquipmentSlot.MAINHAND ? item : equipment.getOrDefault(slot, ItemStack.EMPTY));
        }

        float speed = item.getDestroySpeed(state);
        if (speed > 1.0F) {
            speed += (float) attributes.get(Attributes.MINING_EFFICIENCY).getValue();
        }
        speed *= (float) attributes.get(Attributes.BLOCK_BREAK_SPEED).getValue();
        if (submerged) {
            speed *= (float) attributes.get(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }
        // Haste, fatigue and the airborne penalty are shared by all candidates.
        return speed;
    }

    private static void applyModifiers(Map<Holder<Attribute>, AttributeInstance> attributes, EquipmentSlot slot, ItemStack item) {
        if (item.isEmpty() || item.isBroken()) {
            return;
        }
        // forEachModifier applies item components first, then slot-matching enchantments.
        item.forEachModifier(slot, (attribute, modifier) -> {
            AttributeInstance instance = attributes.get(attribute);
            if (instance != null) {
                instance.removeModifier(modifier.id());
                instance.addTransientModifier(modifier);
            }
        });
    }

    private static Map<EquipmentSlot, ItemStack> getEquipment(Player player) {
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            equipment.put(slot, player.getItemBySlot(slot));
        }
        return equipment;
    }
}