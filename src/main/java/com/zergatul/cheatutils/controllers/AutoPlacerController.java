package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.utils.BlockUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class AutoPlacerController {

    public static final AutoPlacerController instance = new AutoPlacerController();

    private final Minecraft mc = Minecraft.getInstance();

    private AutoPlacerController() {
        //ModApiWrapper.addOnClientTickStart(this::onClientTickStart);
    }

    private void onClientTickStart() {
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (mc.player.getY() == -59) {
            int x = Mth.floor(mc.player.getX());
            int z = Mth.floor(mc.player.getZ());
            ItemStack itemStack = mc.player.getMainHandItem();
            if (itemStack.getItem() == Items.COBBLESTONE) {
                boolean placed = false;
                for (int y = -63; y <= -60 && !placed; y++) {
                    for (int dx = -1; dx <= 1 && !placed; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos pos = new BlockPos(x + dx, y, z + dz);
                            BlockState blockState = mc.level.getBlockState(pos);
                            if (blockState.isAir()) {
                                if (BlockUtils.placeBlock(pos)) {
                                    placed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //
}