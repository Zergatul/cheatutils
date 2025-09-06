package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.AutoToolConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.MultiPlayerGameModeAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class AutoTool implements Module {

    public static final AutoTool instance = new AutoTool();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean skip;

    private AutoTool() {
        Events.StartDestroyBlock.add(this::onStartDestroyBlock);
    }

    public void enterSkipMode() {
        skip = true;
    }

    public void exitSkipMode() {
        skip = false;
    }

    public void selectToolFor(BlockPos pos) {
        selectToolFor(ConfigStore.instance.getConfig().autoTool, pos);
    }

    private void onStartDestroyBlock(BlockPos pos) {
        if (skip) {
            return;
        }

        AutoToolConfig config = ConfigStore.instance.getConfig().autoTool;
        if (!config.enabled) {
            return;
        }

        selectToolFor(config, pos);
    }

    private void selectToolFor(AutoToolConfig config, BlockPos pos) {
        if (mc.level == null || mc.player == null || mc.gameMode == null) {
            return;
        }

        Inventory inventory = mc.player.getInventory();
        BlockState state = mc.level.getBlockState(pos);

        if (config.mode.equals(AutoToolConfig.MODE_HOTBAR)) {
            List<InventoryEntry> entries = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) {
                entries.add(i, new InventoryEntry(i, inventory.getItem(i)));
            }
            moveSelectedToStart(entries, inventory.getSelectedSlot());

            InventoryEntry entry = findBest(config, entries, state);
            if (entry != null) {
                inventory.setSelectedSlot(entry.index);
                ((MultiPlayerGameModeAccessor) mc.gameMode).ensureHasSentCarriedItem_CU();
            }
        }

        if (config.mode.equals(AutoToolConfig.MODE_INVENTORY)) {
            List<InventoryEntry> entries = new ArrayList<>(36);
            for (int i = 0; i < 36; i++) {
                entries.add(i, new InventoryEntry(i, inventory.getItem(i)));
            }
            moveSelectedToStart(entries, inventory.getSelectedSlot());

            InventoryEntry entry = findBest(config, entries, state);
            if (entry != null) {
                if (entry.index < 9) {
                    inventory.setSelectedSlot(entry.index);
                    ((MultiPlayerGameModeAccessor) mc.gameMode).ensureHasSentCarriedItem_CU();
                } else {
                    InventoryUtils.moveItemStack(new InventorySlot(entry.index), new InventorySlot(config.slot - 1));
                    inventory.setSelectedSlot(config.slot - 1);
                    ((MultiPlayerGameModeAccessor) mc.gameMode).ensureHasSentCarriedItem_CU();
                }
            }
        }
    }

    private void moveSelectedToStart(List<InventoryEntry> entries, int selected) {
        if (selected == 0) {
            return;
        }

        entries.addFirst(entries.remove(selected));
    }

    private InventoryEntry findBest(AutoToolConfig config, List<InventoryEntry> entries, BlockState state) {
        double bestMiningSpeed = 0;
        InventoryEntry bestEntry = null;
        for (InventoryEntry entry : entries) {
            if (entry.item.isDamageableItem()) {
                if (entry.item.getMaxDamage() - entry.item.getDamageValue() < config.minDurability) {
                    continue;
                }
            }

            double miningSpeed = getMiningSpeed(state, entry.item);
            boolean isBetter = miningSpeed > bestMiningSpeed;
            if (!isBetter && miningSpeed == bestMiningSpeed) {
                if (bestEntry != null && bestEntry.item.isDamageableItem() && !entry.item.isDamageableItem()) {
                    isBetter = true;
                }
            }

            if (isBetter) {
                bestMiningSpeed = miningSpeed;
                bestEntry = entry;
            }
        }

        return bestEntry;
    }

    private double getMiningSpeed(BlockState state, ItemStack item) {
        double speed = item.getDestroySpeed(state);
        if (speed > 1.0F) {
            // copied from AttributeInstance.calculateValue
            ItemEnchantments enchantments = item.getEnchantments();
            double value1 = Attributes.MINING_EFFICIENCY.value().getDefaultValue();

            for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                for (EnchantmentAttributeEffect effect : enchantment.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES)) {
                    if (effect.attribute() == Attributes.MINING_EFFICIENCY) {
                        if (effect.operation() == AttributeModifier.Operation.ADD_VALUE) {
                            value1 += effect.amount().calculate(enchantments.getLevel(enchantment));
                        }
                    }
                }
            }

            double value2 = value1;
            for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                for (EnchantmentAttributeEffect effect : enchantment.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES)) {
                    if (effect.attribute() == Attributes.MINING_EFFICIENCY) {
                        if (effect.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                            value2 += value1 * effect.amount().calculate(enchantments.getLevel(enchantment));
                        }
                    }
                }
            }
            for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                for (EnchantmentAttributeEffect effect : enchantment.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES)) {
                    if (effect.attribute() == Attributes.MINING_EFFICIENCY) {
                        if (effect.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                            value2 *= 1 + effect.amount().calculate(enchantments.getLevel(enchantment));
                        }
                    }
                }
            }

            speed += Attributes.MINING_EFFICIENCY.value().sanitizeValue(value2);
        }

        return speed;
    }

    private record InventoryEntry(int index, ItemStack item) {}
}