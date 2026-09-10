package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.esp.BlockEsp;
import com.zergatul.cheatutils.modules.esp.blocks.BlockFinder;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.utils.ResourceLocationHelper;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.awt.*;
import java.util.NoSuchElementException;
import java.util.Set;

@SuppressWarnings("unused")
public class BlocksApi {

    @MethodDescription("Checks if Block ESP rendering is enabled")
    public boolean isEnabled() {
        return BlockEsp.INSTANCE.isEnabled();
    }

    @MethodDescription("Toggles Block ESP rendering")
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        BlockEsp.INSTANCE.toggle();
    }

    @MethodDescription(
            "Returns all block ids you have configured for Block ESP. Does not skip disabled ones.\n" +
            "If entry is a group entry - only first block from group will be present in the result.")
    public String[] getEntries() {
        return ConfigStore.instance.getConfig().blocks.getBlockConfigs().stream()
                .map(c -> c.blocks.stream().findFirst().orElseThrow(NoSuchElementException::new))
                .map(b -> ForgeRegistries.BLOCKS.getKey(b).toString())
                .toArray(String[]::new);
    }

    @MethodDescription("Checks if block is enabled. If block is part of a group, returns status of this group")
    public boolean isEnabled(String blockId) {
        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    @MethodDescription("Toggles block enabled status. If block is part of a group, toggles status of entire group")
    @ApiVisibility(ApiType.UPDATE)
    public void toggle(String blockId) {
        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return;
        }

        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setTracerColor(String blockId, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return;
        }

        config.tracerColor = colorValue;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setBoundingBoxColor(String blockId, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return;
        }

        config.boundingBoxColor = colorValue;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setOverlayColor(String blockId, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return;
        }

        config.overlayColor = colorValue;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Rescans chunks. Use it when you face some problems from Block ESP. Normally you should not have problems")
    @ApiVisibility(ApiType.UPDATE)
    public void rescan() {
        BlockFinder.instance.rescan();
    }

    @MethodDescription("Returns blocks count which are tracked by Block ESP. If block is part of a group, returns count of entire group")
    public int getCount(String blockId) {
        ResourceLocation location = ResourceLocationHelper.parseSafe(blockId);
        Block block = ForgeRegistries.BLOCKS.getValue(location);
        if (block == null) {
            return Integer.MIN_VALUE;
        }

        BlockEspConfig config = ConfigStore.instance.getConfig().blocks.find(block);
        if (config == null) {
            return 0;
        }

        Set<BlockPos> set = BlockFinder.instance.blocks.get(config);
        if (set == null) {
            return 0;
        } else {
            return set.size();
        }
    }

    private BlockEspConfig getConfig(String blockId) {
        ResourceLocation location = ResourceLocationHelper.parseSafe(blockId);
        Block block = ForgeRegistries.BLOCKS.getValue(location);
        if (block == null) {
            return null;
        }

        return ConfigStore.instance.getConfig().blocks.find(block);
    }
}