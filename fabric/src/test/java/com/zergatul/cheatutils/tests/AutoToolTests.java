package com.zergatul.cheatutils.tests;

import com.google.gson.Gson;
import com.zergatul.cheatutils.configs.AutoToolConfig;
import com.zergatul.cheatutils.modules.automation.AutoTool;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AutoToolTests {

    @BeforeAll
    public static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void harvestableToolBeatsFasterUnsuitableTool() throws Exception {
        ItemStack gold = tool(Blocks.DIAMOND_ORE, 12, false);
        ItemStack diamond = tool(Blocks.DIAMOND_ORE, 8, true);

        assertSame(diamond, select(Blocks.DIAMOND_ORE, gold, diamond));
        assertSame(diamond, select(Blocks.DIAMOND_ORE, diamond, gold));
    }

    @Test
    public void harvestabilityWinsEvenAfterWrongToolPenalty() throws Exception {
        ItemStack unsuitable = tool(Blocks.DIAMOND_ORE, 100, false);
        ItemStack suitable = tool(Blocks.DIAMOND_ORE, 6, true);

        assertSame(suitable, select(Blocks.DIAMOND_ORE, unsuitable, suitable));
        assertSame(suitable, select(Blocks.DIAMOND_ORE, suitable, unsuitable));
    }

    @Test
    public void speedPriorityStillAccountsForWrongToolPenalty() throws Exception {
        AutoToolConfig config = new AutoToolConfig();
        config.priority = AutoToolConfig.PRIORITY_SPEED;
        ItemStack gold = tool(Blocks.DIAMOND_ORE, 12, false);
        ItemStack diamond = tool(Blocks.DIAMOND_ORE, 8, true);

        assertSame(diamond, select(config, Blocks.DIAMOND_ORE, gold, diamond));
        assertSame(diamond, select(config, Blocks.DIAMOND_ORE, diamond, gold));
    }

    @Test
    public void speedPriorityAllowsFasterUnsuitableTool() throws Exception {
        AutoToolConfig config = new AutoToolConfig();
        config.priority = AutoToolConfig.PRIORITY_SPEED;
        ItemStack goldEfficiencyFive = tool(Blocks.DIAMOND_ORE, 38, false);
        ItemStack iron = tool(Blocks.DIAMOND_ORE, 6, true);

        assertSame(goldEfficiencyFive, select(config, Blocks.DIAMOND_ORE, goldEfficiencyFive, iron));
        assertSame(goldEfficiencyFive, select(config, Blocks.DIAMOND_ORE, iron, goldEfficiencyFive));
        config.priority = AutoToolConfig.PRIORITY_DROP;
        assertSame(iron, select(config, Blocks.DIAMOND_ORE, goldEfficiencyFive, iron));
    }

    @Test
    public void priorityDefaultsAndSerialization() {
        Gson gson = new Gson();
        AutoToolConfig config = gson.fromJson("{\"mode\":\"HOTBAR\"}", AutoToolConfig.class);
        config.sanitize();
        assertEquals(AutoToolConfig.PRIORITY_DROP, config.priority);

        config.priority = AutoToolConfig.PRIORITY_SPEED;
        AutoToolConfig loaded = gson.fromJson(gson.toJson(config), AutoToolConfig.class);
        loaded.sanitize();
        assertEquals(AutoToolConfig.PRIORITY_SPEED, loaded.priority);
    }

    @Test
    public void invalidPriorityFallsBackToDropPriority() {
        AutoToolConfig config = new AutoToolConfig();
        config.priority = null;
        config.sanitize();
        assertEquals(AutoToolConfig.PRIORITY_DROP, config.priority);

        config.priority = "INVALID";
        config.sanitize();
        assertEquals(AutoToolConfig.PRIORITY_DROP, config.priority);
    }

    @Test
    public void fastestHarvestableToolWins() throws Exception {
        ItemStack iron = tool(Blocks.DIAMOND_ORE, 6, true);
        ItemStack diamond = tool(Blocks.DIAMOND_ORE, 8, true);

        assertSame(diamond, select(Blocks.DIAMOND_ORE, iron, diamond));
    }

    @Test
    public void blocksWithoutToolRequirementUseSpeed() throws Exception {
        ItemStack slower = tool(Blocks.DIRT, 4, true);
        ItemStack faster = tool(Blocks.DIRT, 8, false);

        assertFalse(Blocks.DIRT.defaultBlockState().requiresCorrectToolForDrops());
        assertSame(faster, select(Blocks.DIRT, slower, faster));
    }

    @Test
    public void fastestUnsuitableToolIsFallback() throws Exception {
        ItemStack slower = tool(Blocks.DIAMOND_ORE, 4, false);
        ItemStack faster = tool(Blocks.DIAMOND_ORE, 12, false);

        assertSame(faster, select(Blocks.DIAMOND_ORE, slower, faster));
    }

    @Test
    public void lowDurabilityHarvestableToolIsExcluded() throws Exception {
        ItemStack unsuitable = tool(Blocks.DIAMOND_ORE, 12, false);
        ItemStack worn = tool(Blocks.DIAMOND_ORE, 8, true);
        worn.setDamageValue(worn.getMaxDamage() - 9);

        assertSame(unsuitable, select(Blocks.DIAMOND_ORE, worn, unsuitable));
    }

    @Test
    public void equalSpeedStillPreservesExistingPreferences() throws Exception {
        ItemStack first = tool(Blocks.DIAMOND_ORE, 8, true);
        ItemStack second = tool(Blocks.DIAMOND_ORE, 8, true);
        assertSame(first, select(Blocks.DIAMOND_ORE, first, second));

        ItemStack damageable = tool(Blocks.DIRT, 1, false);
        ItemStack empty = ItemStack.EMPTY;
        assertSame(empty, select(Blocks.DIRT, damageable, empty));
    }

    private static ItemStack tool(Block block, float speed, boolean correctForDrops) {
        // Direct holders and explicit components avoid requiring datapack loading.
        ItemStack stack = new ItemStack(Holder.direct(Items.DIAMOND_PICKAXE));
        stack.set(DataComponents.MAX_DAMAGE, 1561);
        stack.set(DataComponents.DAMAGE, 0);
        stack.set(DataComponents.TOOL, new Tool(
                List.of(new Tool.Rule(
                        HolderSet.direct(block.builtInRegistryHolder()),
                        Optional.of(speed),
                        Optional.of(correctForDrops))),
                1, 1, true));
        return stack;
    }

    private static ItemStack select(Block block, ItemStack... stacks) throws Exception {
        return select(new AutoToolConfig(), block, stacks);
    }

    private static ItemStack select(AutoToolConfig config, Block block, ItemStack... stacks) throws Exception {
        Class<?> entryClass = Class.forName(AutoTool.class.getName() + "$InventoryEntry");
        var constructor = entryClass.getDeclaredConstructor(int.class, ItemStack.class);
        constructor.setAccessible(true);
        List<Object> entries = new ArrayList<>();
        for (int i = 0; i < stacks.length; i++) {
            entries.add(constructor.newInstance(i, stacks[i]));
        }

        var findBest = AutoTool.class.getDeclaredMethod(
                "findBest", AutoToolConfig.class, List.class, BlockState.class);
        findBest.setAccessible(true);
        Object selected = findBest.invoke(AutoTool.instance, config, entries, block.defaultBlockState());
        if (selected == null) {
            return null;
        }
        var item = entryClass.getDeclaredMethod("item");
        item.setAccessible(true);
        return (ItemStack) item.invoke(selected);
    }
}