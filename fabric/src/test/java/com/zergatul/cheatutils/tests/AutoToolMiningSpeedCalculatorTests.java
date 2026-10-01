package com.zergatul.cheatutils.tests;

import com.zergatul.cheatutils.utils.AutoToolMiningSpeedCalculator;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AutoToolMiningSpeedCalculatorTests {

    @BeforeAll
    public static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void vanillaOperationOrderAndPlayerBaseAreUsed() {
        AttributeMap attributes = attributes();
        attributes.getInstance(Attributes.MINING_EFFICIENCY).setBaseValue(3);
        ItemStack item = tool(6);
        item.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, modifier("total", 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.MINING_EFFICIENCY, modifier("base", 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.MINING_EFFICIENCY, modifier("add", 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());

        // (3 + 2) * 1.5 * 2 = 15 efficiency, plus 6 tool speed.
        assertEquals(21, calculator(attributes).getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
    }

    @Test
    public void itemComponentsAndApplicableEnchantmentsAreCombined() {
        ItemStack item = tool(6);
        item.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, modifier("component", 10, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
        enchant(item, Attributes.MINING_EFFICIENCY, 5, AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);

        assertEquals(21, calculator(attributes()).getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
    }

    @Test
    public void modifiersForOtherSlotsAreIgnoredOnCandidate() {
        ItemStack item = tool(6);
        item.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, modifier("offhand", 100, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND)
                .add(Attributes.BLOCK_BREAK_SPEED, modifier("head", 100, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                .build());
        enchant(item, Attributes.MINING_EFFICIENCY, 100, AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.OFFHAND);

        assertEquals(6, calculator(attributes()).getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
    }

    @Test
    public void onlyBaseValuesAndActualEquipmentContributeWithoutMutatingPlayerAttributes() {
        AttributeMap attributes = attributes();
        attributes.getInstance(Attributes.MINING_EFFICIENCY).setBaseValue(2);
        AttributeModifier ambient = modifier("ambient", 3, AttributeModifier.Operation.ADD_VALUE);
        AttributeModifier armor = modifier("armor", 7, AttributeModifier.Operation.ADD_VALUE);
        AttributeModifier oldTool = modifier("old_tool", 20, AttributeModifier.Operation.ADD_VALUE);
        attributes.getInstance(Attributes.MINING_EFFICIENCY).addTransientModifier(ambient);
        attributes.getInstance(Attributes.MINING_EFFICIENCY).addTransientModifier(armor);
        attributes.getInstance(Attributes.MINING_EFFICIENCY).addTransientModifier(oldTool);
        ItemStack oldItem = tool(6);
        oldItem.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, oldTool, EquipmentSlotGroup.MAINHAND).build());
        ItemStack helmet = tool(1);
        helmet.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, armor, EquipmentSlotGroup.HEAD).build());
        ItemStack current = tool(8);
        AutoToolMiningSpeedCalculator calculator = new AutoToolMiningSpeedCalculator(
                attributes, Map.of(EquipmentSlot.MAINHAND, current, EquipmentSlot.HEAD, helmet), false);

        // The ambient modifier is deliberately omitted, and the armor bonus is rebuilt.
        assertEquals(17, calculator.getMiningSpeed(Blocks.STONE.defaultBlockState(), current));
        assertEquals(35, calculator.getMiningSpeed(Blocks.STONE.defaultBlockState(), oldItem));
        assertEquals(17, calculator.getMiningSpeed(Blocks.STONE.defaultBlockState(), current));
        assertEquals(32, attributes.getInstance(Attributes.MINING_EFFICIENCY).getValue());
        assertEquals(3, attributes.getInstance(Attributes.MINING_EFFICIENCY).getModifiers().size());
    }

    @Test
    public void sharedIdsCannotChangeWinnerAfterToolSwitchOrAttributeSync() {
        AttributeMap attributes = attributes();
        AttributeModifier bonus = modifier("shared", 5, AttributeModifier.Operation.ADD_VALUE);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, bonus, EquipmentSlotGroup.HAND).build();
        ItemStack offhand = tool(6);
        offhand.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        ItemStack inventoryTool = tool(6);
        inventoryTool.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        ItemStack ordinary = tool(8);
        for (ItemStack held : List.of(inventoryTool, ordinary)) {
            for (boolean syncedBonus : List.of(true, false)) {
                attributes.getInstance(Attributes.MINING_EFFICIENCY).removeModifiers();
                if (syncedBonus) {
                    attributes.getInstance(Attributes.MINING_EFFICIENCY).addTransientModifier(bonus);
                }
                AutoToolMiningSpeedCalculator calculator = new AutoToolMiningSpeedCalculator(
                        attributes, Map.of(EquipmentSlot.MAINHAND, held, EquipmentSlot.OFFHAND, offhand), false);

                // Rebuild the offhand bonus even when vanilla removed the shared ID on a switch.
                assertEquals(13, calculator.getMiningSpeed(Blocks.STONE.defaultBlockState(), ordinary));
                assertEquals(11, calculator.getMiningSpeed(Blocks.STONE.defaultBlockState(), inventoryTool));
                assertEquals(13, calculator.getMiningSpeed(Blocks.STONE.defaultBlockState(), ordinary));
            }
        }
    }

    @Test
    public void equipmentUsesVanillaSlotOrderAndReplacesCollidingOperations() {
        ItemStack candidate = tool(6);
        candidate.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, modifier("shared", 100, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
        ItemStack offhand = tool(1);
        offhand.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, modifier("shared", 2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.OFFHAND)
                .build());
        ItemStack boots = tool(1);
        boots.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, modifier("shared", 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET)
                .build());
        ItemStack helmet = tool(1);
        helmet.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, modifier("shared", 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                .build());
        // Input map order must not control modifier precedence.
        Map<EquipmentSlot, ItemStack> equipment = new LinkedHashMap<>();
        equipment.put(EquipmentSlot.HEAD, helmet);
        equipment.put(EquipmentSlot.FEET, boots);
        equipment.put(EquipmentSlot.OFFHAND, offhand);

        assertEquals(13, new AutoToolMiningSpeedCalculator(attributes(), equipment, false)
                .getMiningSpeed(Blocks.STONE.defaultBlockState(), candidate));
        helmet.setDamageValue(helmet.getMaxDamage());
        assertEquals(10, new AutoToolMiningSpeedCalculator(attributes(), equipment, false)
                .getMiningSpeed(Blocks.STONE.defaultBlockState(), candidate));
        equipment.remove(EquipmentSlot.FEET);
        assertEquals(6, new AutoToolMiningSpeedCalculator(attributes(), equipment, false)
                .getMiningSpeed(Blocks.STONE.defaultBlockState(), candidate));
    }

    @Test
    public void contextSnapshotsBaseValuesAndEquipment() {
        AttributeMap attributes = attributes();
        attributes.getInstance(Attributes.MINING_EFFICIENCY).setBaseValue(2);
        attributes.getInstance(Attributes.BLOCK_BREAK_SPEED).setBaseValue(2);
        attributes.getInstance(Attributes.SUBMERGED_MINING_SPEED).setBaseValue(0.5);
        ItemStack helmet = tool(1);
        // Equipment enchantments are reconstructed too, not just item components.
        enchant(helmet, Attributes.MINING_EFFICIENCY, 7, AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.HEAD);
        Map<EquipmentSlot, ItemStack> equipment = new LinkedHashMap<>();
        equipment.put(EquipmentSlot.HEAD, helmet);
        AutoToolMiningSpeedCalculator calculator = new AutoToolMiningSpeedCalculator(attributes, equipment, true);

        attributes.getInstance(Attributes.MINING_EFFICIENCY).setBaseValue(100);
        attributes.getInstance(Attributes.BLOCK_BREAK_SPEED).setBaseValue(100);
        attributes.getInstance(Attributes.SUBMERGED_MINING_SPEED).setBaseValue(20);
        helmet.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        equipment.clear();

        assertEquals(15, calculator.getMiningSpeed(Blocks.STONE.defaultBlockState(), tool(6)));
    }

    @Test
    public void duplicateModifierIdReplacesPreviousOperation() {
        ItemStack item = tool(6);
        EnchantmentAttributeEffect effect = enchant(item, Attributes.BLOCK_BREAK_SPEED, 1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, EquipmentSlotGroup.MAINHAND);
        item.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.BLOCK_BREAK_SPEED, new AttributeModifier(
                        effect.getModifier(1, EquipmentSlot.MAINHAND).id(), 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());

        assertEquals(12, calculator(attributes()).getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
    }

    @Test
    public void submergedMiningModifiersApplyOnlyUnderwater() {
        ItemStack item = tool(6);
        item.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.SUBMERGED_MINING_SPEED, modifier("water", 0.8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
        AutoToolMiningSpeedCalculator underwater = new AutoToolMiningSpeedCalculator(attributes(), Map.of(), true);
        ItemStack ordinary = tool(8);

        assertEquals(6, underwater.getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
        assertEquals(1.6, underwater.getMiningSpeed(Blocks.STONE.defaultBlockState(), ordinary), 0.000001);
        assertEquals(6, calculator(attributes()).getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
    }

    @Test
    public void efficiencyGateBrokenItemsAndAttributeBoundsAreRespected() {
        ItemStack item = tool(1);
        item.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.MINING_EFFICIENCY, modifier("efficiency", 2000, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
        assertEquals(1, calculator(attributes()).getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
        item.set(DataComponents.TOOL, new Tool(List.of(), 6, 1, true));
        assertEquals(1030, calculator(attributes()).getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
        item.setDamageValue(item.getMaxDamage());
        assertEquals(6, calculator(attributes()).getMiningSpeed(Blocks.STONE.defaultBlockState(), item));
    }

    private static AttributeMap attributes() {
        return new AttributeMap(AttributeSupplier.builder()
                .add(Attributes.MINING_EFFICIENCY)
                .add(Attributes.BLOCK_BREAK_SPEED)
                .add(Attributes.SUBMERGED_MINING_SPEED)
                .build());
    }

    private static ItemStack tool(float speed) {
        ItemStack item = new ItemStack(Holder.direct(Items.DIAMOND_PICKAXE));
        item.set(DataComponents.MAX_DAMAGE, 1561);
        item.set(DataComponents.DAMAGE, 0);
        item.set(DataComponents.TOOL, new Tool(List.of(), speed, 1, true));
        return item;
    }

    private static AttributeModifier modifier(String id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(Identifier.fromNamespaceAndPath("test", id), amount, operation);
    }

    private static AutoToolMiningSpeedCalculator calculator(AttributeMap attributes) {
        return new AutoToolMiningSpeedCalculator(attributes, Map.of(), false);
    }

    private static EnchantmentAttributeEffect enchant(
            ItemStack item, Holder<Attribute> attribute, float amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot
    ) {
        EnchantmentAttributeEffect effect = new EnchantmentAttributeEffect(
                Identifier.fromNamespaceAndPath("test", "enchantment"), attribute, LevelBasedValue.constant(amount), operation);
        Enchantment enchantment = Enchantment.enchantment(Enchantment.definition(
                        HolderSet.direct(Items.DIAMOND_PICKAXE.builtInRegistryHolder()), 1, 1,
                        Enchantment.constantCost(1), Enchantment.constantCost(1), 1, slot))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES, effect)
                .build(Identifier.fromNamespaceAndPath("test", "enchantment"));
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(Holder.direct(enchantment), 1);
        item.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        return effect;
    }
}