package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.configs.AdvancedTooltipsConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class AdvancedTooltips implements Module {

    public static final AdvancedTooltips instance = new AdvancedTooltips();

    private AdvancedTooltips() {
        Events.GatherTooltipComponents.add(this::onGatherTooltipComponents);
    }

    private void onGatherTooltipComponents(GatherTooltipComponentsEvent event) {
        AdvancedTooltipsConfig config = ConfigStore.instance.getConfig().advancedTooltipsConfig;
        handleBeeContainer(config, event);
        handleRepairCost(config, event);
    }

    private void handleBeeContainer(AdvancedTooltipsConfig config, GatherTooltipComponentsEvent event) {
        if (config.beeContainer) {
            ItemStack itemStack = event.itemStack();
            Item item = itemStack.getItem();
            if (item == Items.BEE_NEST || item == Items.BEEHIVE) {
                int beesCount = 0;
                int honeyLevel = 0;

                List<BeehiveBlockEntity.Occupant> bees = itemStack.get(DataComponents.BEES);
                if (bees != null) {
                    beesCount = bees.size();
                }

                BlockItemStateProperties properties = itemStack.get(DataComponents.BLOCK_STATE);
                if (properties != null) {
                    Integer value = properties.get(BlockStateProperties.LEVEL_HONEY);
                    if (value != null) {
                        honeyLevel = value;
                    }
                }

                event.list().add(MutableComponent.create(new PlainTextContents.LiteralContents("Bees count: " + beesCount)).withStyle(ChatFormatting.GRAY));
                event.list().add(MutableComponent.create(new PlainTextContents.LiteralContents("Honey level: " + honeyLevel)).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private void handleRepairCost(AdvancedTooltipsConfig config, GatherTooltipComponentsEvent event) {
        if (config.repairCost) {
            ItemStack itemStack = event.itemStack();
            int cost = itemStack.getOrDefault(DataComponents.REPAIR_COST, 0);
            if (cost > 0) {
                event.list().add(MutableComponent.create(new PlainTextContents.LiteralContents("Repair cost: " + cost)).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}