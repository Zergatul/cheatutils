package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.utils.HotbarSlot;
import net.minecraft.world.InteractionHand;

import java.util.concurrent.CompletableFuture;

public interface SimpleBlockPlan {
    CompletableFuture<Boolean> apply(HotbarSlot slot, boolean silentSwitch);
}