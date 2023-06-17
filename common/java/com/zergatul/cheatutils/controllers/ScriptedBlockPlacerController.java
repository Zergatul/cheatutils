package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ScriptedBlockPlacerConfig;
import com.zergatul.cheatutils.utils.BlockPlacingMethod;
import com.zergatul.cheatutils.utils.BlockUtils;
import com.zergatul.cheatutils.utils.NearbyBlockEnumerator;
import com.zergatul.cheatutils.utils.SlotSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ScriptedBlockPlacerController {

    public static final ScriptedBlockPlacerController instance = new ScriptedBlockPlacerController();

    private final Minecraft mc = Minecraft.getInstance();
    private final SlotSelector slotSelector = new SlotSelector();
    private Runnable script;
    private String blockId;
    private BlockPlacingMethod method;

    private ScriptedBlockPlacerController() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    public void setScript(Runnable script) {
        this.script = script;
    }

    public void setBlock(String blockId, BlockPlacingMethod method) {
        this.blockId = blockId;
        this.method = method;
    }

    private void onClientTickEnd() {
        ScriptedBlockPlacerConfig config = ConfigStore.instance.getConfig().scriptedBlockPlacerConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.player == null || mc.level == null) {
            return;
        }

        if (script == null) {
            return;
        }

        Vec3 eyePos = mc.player.getEyePosition(1);
        for (BlockPos pos : NearbyBlockEnumerator.getPositions(eyePos, config.maxRange)) {
            BlockState state = mc.level.getBlockState(pos);
            if (!state.canBeReplaced()) {
                continue;
            }

            blockId = null;
            CurrentBlockController.instance.set(pos, state);
            script.run();
            CurrentBlockController.instance.clear();

            if (blockId == null) {
                continue;
            }

            Block block = Registries.BLOCKS.getValue(new ResourceLocation(blockId));
            if (block == Blocks.AIR) {
                continue;
            }

            int slot = slotSelector.selectBlock(config, block);
            if (slot < 0) {
                continue;
            }

            BlockUtils.PlaceBlockPlan plan = BlockUtils.getPlacingPlan(pos, config.attachToAir, method);
            if (plan != null) {
                mc.player.getInventory().selected = slot;
                BlockUtils.applyPlacingPlan(plan, config.useShift);
                return;
            }
        }
    }
}