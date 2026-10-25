package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AdvancedTooltipsConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AdvancedTooltips implements Module {

    public static final AdvancedTooltips INSTANCE = new AdvancedTooltips();

    private final SettingHandle2<AdvancedTooltipsConfig> settings = Settings.get(AdvancedTooltipsConfig.class);

    private AdvancedTooltips() {
        Events.GatherTooltipComponents.add(this::onGatherTooltipComponents);

        WebApiRegistry.INSTANCE.register("advanced-tooltips", settings, AdvancedTooltipsConfig.class);
    }

    public SettingHandle2<AdvancedTooltipsConfig> getSettings() {
        return settings;
    }

    private void onGatherTooltipComponents(GatherTooltipComponentsEvent event) {
        AdvancedTooltipsConfig config = settings.get();
        handleBeeContainer(config, event);
        handleRepairCost(config, event);
    }

    private void handleBeeContainer(AdvancedTooltipsConfig config, GatherTooltipComponentsEvent event) {
        if (config.beeContainer()) {
            ItemStack itemStack = event.itemStack();
            Item item = itemStack.getItem();
            if (item == Items.BEE_NEST || item == Items.BEEHIVE) {
                int beesCount = 0;
                int honeyLevel = 0;

                Bees bees = itemStack.get(DataComponents.BEES);
                if (bees != null) {
                    beesCount = bees.bees().size();
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
        if (config.repairCost()) {
            ItemStack itemStack = event.itemStack();
            int cost = itemStack.getOrDefault(DataComponents.REPAIR_COST, 0);
            if (cost > 0) {
                event.list().add(MutableComponent.create(new PlainTextContents.LiteralContents("Repair cost: " + cost)).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}