package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ContinueDestroyBlockEvent;
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
        Events.ContinueDestroyBlock.add(this::onContinueDestroyBlock);
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

    private void onContinueDestroyBlock(ContinueDestroyBlockEvent event) {
        if (skip) {
            return;
        }

        AutoToolConfig config = ConfigStore.instance.getConfig().autoTool;
        if (!config.enabled) {
            return;
        }

        if (selectToolFor(config, event.pos)) {
            event.markNewDestroy = true;
        }
    }

    // returns true when selected item was changed
    private boolean selectToolFor(AutoToolConfig config, BlockPos pos) {
        if (mc.level == null || mc.player == null || mc.gameMode == null) {
            return false;
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
            if (entry != null && entry.index != inventory.getSelectedSlot()) {
                inventory.setSelectedSlot(entry.index);
                ((MultiPlayerGameModeAccessor) mc.gameMode).ensureHasSentCarriedItem_CU();
                return true;
            }

            return false;
        }

        if (config.mode.equals(AutoToolConfig.MODE_INVENTORY)) {
            List<InventoryEntry> entries = new ArrayList<>(36);
            for (int i = 0; i < 36; i++) {
                entries.add(i, new InventoryEntry(i, inventory.getItem(i)));
            }
            moveSelectedToStart(entries, inventory.getSelectedSlot());

            InventoryEntry entry = findBest(config, entries, state);
            if (entry != null && entry.index != inventory.getSelectedSlot()) {
                if (entry.index < 9) {
                    inventory.setSelectedSlot(entry.index);
                    ((MultiPlayerGameModeAccessor) mc.gameMode).ensureHasSentCarriedItem_CU();
                } else {
                    InventoryUtils.moveItemStack(new InventorySlot(entry.index), new InventorySlot(config.slot - 1));
                    inventory.setSelectedSlot(config.slot - 1);
                    ((MultiPlayerGameModeAccessor) mc.gameMode).ensureHasSentCarriedItem_CU();
                }

                return true;
            }

            return false;
        }

        throw new IllegalStateException();
    }

    private void moveSelectedToStart(List<InventoryEntry> entries, int selected) {
        if (selected == 0) {
            return;
        }

        entries.addFirst(entries.remove(selected));
    }

    private InventoryEntry findBest(AutoToolConfig config, List<InventoryEntry> entries, BlockState state) {
        boolean prioritizeDrops = AutoToolConfig.PRIORITY_DROP.equals(config.priority);
        double bestMiningSpeed = 0;
        boolean bestCanHarvest = false;
        InventoryEntry bestEntry = null;
        for (InventoryEntry entry : entries) {
            if (entry.item.isDamageableItem()) {
                if (entry.item.getMaxDamage() - entry.item.getDamageValue() < config.minDurability) {
                    continue;
                }
            }

            boolean canHarvest = !state.requiresCorrectToolForDrops() || entry.item.isCorrectToolForDrops(state);
            // Block hardness is the same for every candidate; use vanilla's correct-tool divisor.
            double miningSpeed = getMiningSpeed(state, entry.item) / (canHarvest ? 30 : 100);
            // Drop priority preserves drops even when an unsuitable tool would break faster.
            boolean isBetter = bestEntry == null || (prioritizeDrops && canHarvest && !bestCanHarvest);
            boolean samePriority = !prioritizeDrops || canHarvest == bestCanHarvest;
            if (!isBetter && samePriority) {
                isBetter = miningSpeed > bestMiningSpeed;
            }
            if (!isBetter && samePriority && miningSpeed == bestMiningSpeed) {
                if (bestEntry != null && bestEntry.item.isDamageableItem() && !entry.item.isDamageableItem()) {
                    isBetter = true;
                }
            }

            if (isBetter) {
                bestMiningSpeed = miningSpeed;
                bestCanHarvest = canHarvest;
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