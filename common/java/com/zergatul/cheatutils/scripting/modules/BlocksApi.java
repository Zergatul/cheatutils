package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.esp.BlockEsp;
import com.zergatul.cheatutils.modules.esp.BlockFinder;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.types.BlockPosWrapper;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.Set;

@SuppressWarnings("unused")
public class BlocksApi {

    @MethodDescription("""
            Checks if Block ESP rendering is enabled
            """)
    public boolean isEnabled() {
        return BlockEsp.instance.isEnabled();
    }

    @MethodDescription("""
            Toggles Block ESP rendering
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        BlockEsp.instance.toggle();
    }

    @MethodDescription("""
            Returns all block ids you have configured for Block ESP. Does not skip disabled ones.
            If entry is a group entry - only first block from group will be present in the result.
            """)
    public String[] getEntries() {
        return ConfigStore.instance.getConfig().blocks.getBlockConfigs().stream()
                .map(c -> c.blocks.stream().findFirst().orElseThrow())
                .map(b -> Registries.BLOCKS.getKey(b).toString())
                .toArray(String[]::new);
    }

    @MethodDescription("""
            Checks if block is enabled. If block is part of a group, returns status of this group
            """)
    public boolean isEnabled(String blockId) {
        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    @MethodDescription("""
            Toggles block enabled status. If block is part of a group, toggles status of entire group
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle(String blockId) {
        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return;
        }

        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

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

    @MethodDescription("""
            Rescans chunks. Use it when you face some problems from Block ESP. Normally you should not have problems
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void rescan() {
        BlockFinder.instance.rescan();
    }

    @MethodDescription("""
            Returns blocks count which are tracked by Block ESP. If block is part of a group, returns count of entire group
            """)
    public int getCount(String blockId) {
        Identifier location = Identifier.parse(blockId);
        Block block = Registries.BLOCKS.getValue(location);
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

    @MethodDescription("""
            Removes block from Block ESP and prevents future Block ESP at this coordinates.
            Black list is not persistent and it is cleared when you restart Minecraft.
            """)
    public void addBlackList(BlockPosWrapper pos) {
        addBlackList(pos.getX(), pos.getY(), pos.getZ());
    }

    @MethodDescription("""
            Removes block from Block ESP and prevents future Block ESP at this coordinates.
            Black list is not persistent and it is cleared when you restart Minecraft.
            """)
    public void addBlackList(int x, int y, int z) {
        BlockFinder.instance.addBlackList(new BlockPos(x, y, z));
    }

    @MethodDescription("""
            Clears black list with block coordinates. This action does not re-add blocks that match Block ESP conditions at these coordinates.
            """)
    public void clearBlackList() {
        BlockFinder.instance.blackList.clear();
    }

    @MethodDescription("""
            Adds specified block position for ESP.
            """)
    public void addCustom(BlockPosWrapper pos, String color) {
        addCustom(pos.getX(), pos.getY(), pos.getZ(), color);
    }

    @MethodDescription("""
            Adds specified block position for ESP.
            """)
    public void addCustom(int x, int y, int z, String color) {
        Integer colorInt = ColorUtils.parseColor(color);
        if (colorInt != null) {
            BlockEsp.instance.addCustom(new BlockPos(x, y, z), colorInt);
        }
    }

    @MethodDescription("""
            Adds specified block position for ESP.
            """)
    public void removeCustom(BlockPosWrapper pos) {
        removeCustom(pos.getX(), pos.getY(), pos.getZ());
    }

    @MethodDescription("""
            Adds specified block position for ESP.
            """)
    public void removeCustom(int x, int y, int z) {
        BlockEsp.instance.removeCustom(new BlockPos(x, y, z));
    }

    @MethodDescription("""
            Removes all custom block positions.
            """)
    public void clearCustom() {
        BlockEsp.instance.clearCustom();
    }

    private BlockEspConfig getConfig(String blockId) {
        Identifier location = Identifier.parse(blockId);
        Block block = Registries.BLOCKS.getValue(location);
        if (block == null) {
            return null;
        }

        return ConfigStore.instance.getConfig().blocks.find(block);
    }
}