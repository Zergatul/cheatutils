package com.zergatul.cheatutils.blocks;

import java.util.concurrent.CompletableFuture;

public abstract class BlockBreakPlan {
    public abstract CompletableFuture<Void> apply();
}