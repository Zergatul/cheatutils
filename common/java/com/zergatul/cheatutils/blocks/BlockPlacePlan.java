package com.zergatul.cheatutils.blocks;

import net.minecraft.world.InteractionHand;

import java.util.concurrent.CompletableFuture;

public abstract class BlockPlacePlan {

    public CompletableFuture<Void> apply() {
        return apply(InteractionHand.MAIN_HAND);
    }

    public abstract CompletableFuture<Void> apply(InteractionHand hand);
}