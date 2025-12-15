package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockAutomationConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ItemStackPredicate;
import com.zergatul.cheatutils.scripting.types.ItemStackWrapper;
import com.zergatul.cheatutils.blocks.BlockPlacingMethod;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;

@SuppressWarnings("unused")
public class BlockAutomationApi extends ModuleApi<BlockAutomationConfig> {

    @MethodDescription("""
            For debugging
            """)
    public void useOne() {
        BlockAutomation.instance.placeOne();
    }

    @MethodDescription("""
            Uses specified item from your inventory at current coordinates
            """)
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useItem(String itemId) {
        Item item = Registries.ITEMS.safeParse(itemId);
        if (item != null) {
            BlockAutomation.instance.useItem(stack -> stack.is(item), BlockPlacingMethod.ANY);
        }
    }

    @MethodDescription(value = """
            Uses one specified item from your inventory at current coordinates.
            "method" parameter specifies custom way to use block. Allowed values:
                - "bottom-slab"
                - "top-slab"
                - "facing-top"    // for blocks like piston
                - "facing-bottom"
                - "facing-north"
                - "facing-south"
                - "facing-east"
                - "facing-west"
                - "from-top"      // for items like seeds
                - "from-bottom"
                - "from-horizontal"
                - "item-use"      // for items like bonemeal
                - "air-place"
            """)
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useItem(String itemId, String method) {
        Item item = Registries.ITEMS.safeParse(itemId);
        if (item != null) {
            BlockAutomation.instance.useItem(stack -> stack.is(item), BlockPlacingMethod.parse(method));
        }
    }

    @MethodDescription(value = """
            Uses first item from your inventory that matches predicate at current coordinates.
            "method" parameter specifies custom way to use block. Allowed values:
                - "bottom-slab"
                - "top-slab"
                - "facing-top"    // for blocks like piston
                - "facing-bottom"
                - "facing-north"
                - "facing-south"
                - "facing-east"
                - "facing-west"
                - "from-top"      // for items like seeds
                - "from-bottom"
                - "from-horizontal"
                - "item-use"      // for items like bonemeal
                - "air-place"
            """)
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useItem(ItemStackPredicate predicate, String method) {
        BlockAutomation.instance.useItem(stack -> predicate.test(new ItemStackWrapper(stack)), BlockPlacingMethod.parse(method));
    }

    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useWithMainHand() {
        BlockAutomation.instance.useItem(InteractionHand.MAIN_HAND, BlockPlacingMethod.ANY);
    }

    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useWithMainHand(String method) {
        BlockAutomation.instance.useItem(InteractionHand.MAIN_HAND, BlockPlacingMethod.parse(method));
    }

    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useWithOffHand() {
        BlockAutomation.instance.useItem(InteractionHand.OFF_HAND, BlockPlacingMethod.ANY);
    }

    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useWithOffHand(String method) {
        BlockAutomation.instance.useItem(InteractionHand.OFF_HAND, BlockPlacingMethod.parse(method));
    }

    @MethodDescription("""
            Breaks block with currently equipped item
            """)
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void breakBlock() {
        BlockAutomation.instance.breakBlock(stack -> true);
    }

    @MethodDescription("""
            Breaks block with item id you specify
            """)
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void breakBlock(String itemId) {
        Item item = Registries.ITEMS.safeParse(itemId);
        if (item != null) {
            BlockAutomation.instance.breakBlock(stack -> stack.is(item));
        }
    }

    @MethodDescription("""
            Breaks block with item id and enchantment id you specify
            """)
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void breakBlock(String itemId, String enchantmentId) {
        Item item = Registries.ITEMS.safeParse(itemId);
        if (item != null) {
            Identifier enchantment = Identifier.tryParse(enchantmentId);
            if (enchantment != null) {
                BlockAutomation.instance.breakBlock(stack -> {
                    if (!stack.is(item)) {
                        return false;
                    }
                    return stack.getEnchantments().entrySet().stream().anyMatch(holder -> holder.getKey().unwrapKey().get().identifier().equals(enchantment));
                });
            }
        }
    }

    @MethodDescription("""
            Breaks block with an item that matches custom condition
            """)
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void breakBlock(ItemStackPredicate predicate) {
        BlockAutomation.instance.breakBlock(stack -> predicate.test(new ItemStackWrapper(stack)));
    }

    @MethodDescription("""
            Breaks block with tool selected by Auto Tool module.
            Auto Tool module may be disabled, it will still work. Uses current settings of Auto Tool module.
            """)
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void breakBlockAutoTool() {
        BlockAutomation.instance.breakBlockAutoTool();
    }

    @Override
    protected BlockAutomationConfig getConfig() {
        return ConfigStore.instance.getConfig().blockAutomationConfig;
    }
}