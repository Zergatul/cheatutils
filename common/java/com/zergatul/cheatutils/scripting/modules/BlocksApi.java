package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.RegistryExtensions;
import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import com.zergatul.cheatutils.modules.esp.block.BlockEsp;
import com.zergatul.cheatutils.modules.esp.block.BlockFinder;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.types.BlockPosWrapper;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.Set;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class BlocksApi {

    @MethodDescription("""
            Checks if Block ESP rendering is enabled
            """)
    public boolean isEnabled() {
        return BlockEsp.INSTANCE.isEnabled();
    }

    @MethodDescription("""
            Toggles Block ESP rendering
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        BlockEsp.INSTANCE.toggle();
    }

    @MethodDescription("""
            Returns all block ids you have configured for Block ESP. Does not skip disabled ones.
            If entry is a group entry - only first block from group will be present in the result.
            """)
    public String[] getEntries() {
        return BlockEsp.INSTANCE.getSettings().get().entries().stream()
                .map(c -> c.blocks().stream().findFirst().orElseThrow())
                .map(b -> BuiltInRegistries.BLOCK.getKey(b).toString())
                .toArray(String[]::new);
    }

    @MethodDescription("""
            Checks if block is enabled. If block is part of a group, returns status of this group
            """)
    public boolean isEnabled(String blockId) {
        BlockEspEntry entry = getEntry(blockId);
        if (entry == null) {
            return false;
        }
        return entry.enabled();
    }

    @MethodDescription("""
            Toggles block enabled status. If block is part of a group, toggles status of entire group
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle(String blockId) {
        update(blockId, e -> e.withEnabled(!e.enabled()));
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setTracerColor(String blockId, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        update(blockId, e -> e.withTracerColor(colorValue));
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setBoundingBoxColor(String blockId, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        update(blockId, e -> e.withBoundingBoxColor(colorValue));
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setOverlayColor(String blockId, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        update(blockId, e -> e.withOverlayColor(colorValue));
    }

    @MethodDescription("""
            Rescans chunks. Use it when you face some problems from Block ESP. Normally you should not have problems
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void rescan() {
        BlockFinder.INSTANCE.rescan();
    }

    @MethodDescription("""
            Returns blocks count which are tracked by Block ESP. If block is part of a group, returns count of entire group
            """)
    public int getCount(String blockId) {
        Block block = RegistryExtensions.safeParse(BuiltInRegistries.BLOCK, blockId);
        if (block == null) {
            return Integer.MIN_VALUE;
        }

        BlockEspEntry entry = getEntry(blockId);
        if (entry == null) {
            return 0;
        }

        int total = 0;
        for (Block b : entry.blocks()) {
            Set<BlockPos> positions = BlockFinder.INSTANCE.getPositons(b);
            if (positions != null) {
                total += positions.size();
            }
        }

        return total;
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
        BlockFinder.INSTANCE.addBlackList(new BlockPos(x, y, z));
    }

    @MethodDescription("""
            Clears black list with block coordinates. This action does not re-add blocks that match Block ESP conditions at these coordinates.
            """)
    public void clearBlackList() {
        BlockFinder.INSTANCE.clearBlackList();
    }

    @MethodDescription("""
            Adds specified block position for ESP. Color format: #RRGGBB or #RRGGBBAA.
            """)
    public void addCustom(BlockPosWrapper pos, String color) {
        addCustom(pos.getX(), pos.getY(), pos.getZ(), color);
    }

    @MethodDescription("""
            Adds specified block position for ESP. Color format: #RRGGBB or #RRGGBBAA.
            """)
    public void addCustom(int x, int y, int z, String color) {
        Integer colorInt = ColorUtils.parseColor(color);
        if (colorInt != null) {
            BlockEsp.INSTANCE.addCustom(new BlockPos(x, y, z), colorInt);
        }
    }

    @MethodDescription("""
            Removes specified custom block position from ESP.
            """)
    public void removeCustom(BlockPosWrapper pos) {
        removeCustom(pos.getX(), pos.getY(), pos.getZ());
    }

    @MethodDescription("""
            Removes specified custom block position from ESP.
            """)
    public void removeCustom(int x, int y, int z) {
        BlockEsp.INSTANCE.removeCustom(new BlockPos(x, y, z));
    }

    @MethodDescription("""
            Removes all custom block positions.
            """)
    public void clearCustom() {
        BlockEsp.INSTANCE.clearCustom();
    }

    private void update(String blockId, UnaryOperator<BlockEspEntry> map) {
        BlockEsp.INSTANCE.getSettings().update(config -> {
            BlockEspEntry entry = getEntry(config.entries(), blockId);
            if (entry == null) {
                return config;
            }

            ImmutableList<BlockEspEntry> newEntries = config.entries().replaceFirst(e -> e == entry, map);
            return config.withEntries(newEntries);
        });
    }

    private @Nullable BlockEspEntry getEntry(String blockId) {
        return getEntry(BlockEsp.INSTANCE.getSettings().get().entries(), blockId);
    }

    private @Nullable BlockEspEntry getEntry(ImmutableList<BlockEspEntry> entries, String blockId) {
        Block block = RegistryExtensions.safeParse(BuiltInRegistries.BLOCK, blockId);
        if (block == null) {
            return null;
        }

        return entries.stream()
                .filter(e -> e.blocks().contains(block))
                .findFirst()
                .orElse(null);
    }
}