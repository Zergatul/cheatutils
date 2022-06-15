package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Either;
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

public class BeeContainerController {

    public static final BeeContainerController instance = new BeeContainerController();

    private BeeContainerController() {

    }

    @SubscribeEvent
    public void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
        if (!ConfigStore.instance.getConfig().beeContainerTooltipConfig.enabled) {
            return;
        }

        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();
        if (item == Items.BEE_NEST || item == Items.BEEHIVE) {
            int beesCount = 0;
            String honeyLevel = "";
            CompoundTag root = itemStack.getTag();
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