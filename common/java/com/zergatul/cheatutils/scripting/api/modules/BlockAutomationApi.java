package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.BlockAutomationConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.utils.BlockPlacingMethod;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class BlockAutomationApi extends ModuleApi<BlockAutomationConfig> {

    @MethodDescription("For debugging")
    public void useOne() {
        BlockAutomation.instance.placeOne();
    }

    @MethodDescription("Uses specified block item from your inventory at current coordinates")
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useItem(String itemId) {
        BlockAutomation.instance.useItem(itemId, BlockPlacingMethod.ANY);
    }

    @MethodDescription("Uses specified block item from your inventory at current coordinates with a custom placement method")
    @ApiVisibility(ApiType.BLOCK_AUTOMATION)
    public void useItem(String itemId, String method) {
        BlockAutomation.instance.useItem(itemId, parseMethod(method));
    }

    @Override
    protected BlockAutomationConfig getConfig() {
        return ConfigStore.instance.getConfig().blockAutomationConfig;
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
            default -> BlockPlacingMethod.ANY;
        };
    }
}