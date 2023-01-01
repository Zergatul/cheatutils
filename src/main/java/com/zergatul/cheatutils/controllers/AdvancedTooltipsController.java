package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.configs.AdvancedTooltipsConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Function;

public class AdvancedTooltipsController {

    public static final AdvancedTooltipsController instance = new AdvancedTooltipsController();

    private AdvancedTooltipsController() {

    }

    @SubscribeEvent
    public void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
        AdvancedTooltipsConfig config = ConfigStore.instance.getConfig().advancedTooltipsConfig;
        handleBeeContainer(config, event);
        handleRepairCost(config, event);
    }

    private void handleBeeContainer(AdvancedTooltipsConfig config, RenderTooltipEvent.GatherComponents event) {
        if (config.beeContainer) {
            ItemStack itemStack = event.getItemStack();
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

                var function = (Function<FormattedText, Either<FormattedText, TooltipComponent>>) Either::left;
                var components = event.getTooltipElements();
                components.add(function.apply(MutableComponent.create(new LiteralContents("Bees count: " + beesCount)).withStyle(ChatFormatting.GRAY)));
                components.add(function.apply(MutableComponent.create(new LiteralContents("Honey level: " + honeyLevel)).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    private void handleRepairCost(AdvancedTooltipsConfig config, RenderTooltipEvent.GatherComponents event) {
        if (config.repairCost) {
            ItemStack itemStack = event.getItemStack();
            int cost = itemStack.getBaseRepairCost();
            if (cost > 0) {
                var function = (Function<FormattedText, Either<FormattedText, TooltipComponent>>) Either::left;
                var components = event.getTooltipElements();
                components.add(function.apply(MutableComponent.create(new LiteralContents("Repair cost: " + cost)).withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}
