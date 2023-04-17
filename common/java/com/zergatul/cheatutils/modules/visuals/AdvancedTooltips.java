package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.configs.AdvancedTooltipsConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
                String honeyLevel = "";
                CompoundTag root = itemStack.getTag();
                if (root == null) {
                    return;
                }
                if (root.contains("BlockEntityTag")) {
                    CompoundTag blockEntityTag = root.getCompound("BlockEntityTag");
                    if (blockEntityTag.contains("Bees")) {
                        ListTag bees = (ListTag) blockEntityTag.get("Bees");
                        beesCount = bees.size();
                    }
                }
                if (root.contains("BlockStateTag")) {
                    CompoundTag blockStateTag = root.getCompound("BlockStateTag");
                    if (blockStateTag.contains("honey_level")) {
                        honeyLevel = blockStateTag.getString("honey_level");
                    }
                }

                event.list().add(MutableComponent.create(new LiteralContents("Bees count: " + beesCount)).withStyle(ChatFormatting.GRAY));
                event.list().add(MutableComponent.create(new LiteralContents("Honey level: " + honeyLevel)).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private void handleRepairCost(AdvancedTooltipsConfig config, GatherTooltipComponentsEvent event) {
        if (config.repairCost) {
            ItemStack itemStack = event.itemStack();
            int cost = itemStack.getBaseRepairCost();
            if (cost > 0) {
                event.list().add(MutableComponent.create(new LiteralContents("Repair cost: " + cost)).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}