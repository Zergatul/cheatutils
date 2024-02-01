package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.utils.BlockPlacingMethod;

public class BlockAutomationApi {

    public void useOne() {
        BlockAutomation.instance.placeOne();
    }

    @ApiVisibility(ApiType.BLOCK_PLACER)
    public void useItem(String blockId) {
        BlockAutomation.instance.useItem(blockId, BlockPlacingMethod.ANY);
    }

    @ApiVisibility(ApiType.BLOCK_PLACER)
    public void useItem(String[] blockIds) {
        BlockAutomation.instance.useItem(blockIds, BlockPlacingMethod.ANY);
    }

    @ApiVisibility(ApiType.BLOCK_PLACER)
    public void useItem(String itemId, String method) {
        BlockAutomation.instance.useItem(itemId, parseMethod(method));
    }

    @ApiVisibility(ApiType.BLOCK_PLACER)
    public void breakBlock() {
        BlockAutomation.instance.breakBlock(null, null);
    }

    @ApiVisibility(ApiType.BLOCK_PLACER)
    public void breakBlock(String itemId) {
        BlockAutomation.instance.breakBlock(itemId, null);
    }

    @ApiVisibility(ApiType.BLOCK_PLACER)
    public void breakBlock(String itemId, String enchantmentId) {
        BlockAutomation.instance.breakBlock(itemId, enchantmentId);
    }

    private BlockPlacingMethod parseMethod(String value) {
        return switch (value) {
            case "bottom-slab" -> BlockPlacingMethod.BOTTOM_SLAB;
            case "top-slab" -> BlockPlacingMethod.TOP_SLAB;
            case "facing-top" -> BlockPlacingMethod.FACING_TOP;
            case "facing-bottom" -> BlockPlacingMethod.FACING_BOTTOM;
            case "facing-north" -> BlockPlacingMethod.FACING_NORTH;
            case "facing-south" -> BlockPlacingMethod.FACING_SOUTH;
            case "facing-east" -> BlockPlacingMethod.FACING_EAST;
            case "facing-west" -> BlockPlacingMethod.FACING_WEST;
            case "from-top" -> BlockPlacingMethod.FROM_TOP;
            case "item-use" -> BlockPlacingMethod.ITEM_USE;
            case "air-place" -> BlockPlacingMethod.AIR_PLACE;
            default -> BlockPlacingMethod.ANY;
        };
    }
}