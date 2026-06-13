package com.zergatul.cheatutils.blocks;

import net.minecraft.world.InteractionHand;

import java.util.concurrent.CompletableFuture;

public interface SimpleBlockPlan {
    CompletableFuture<Boolean> apply(InteractionHand hand);
}