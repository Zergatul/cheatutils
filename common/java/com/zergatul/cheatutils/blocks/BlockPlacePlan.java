package com.zergatul.cheatutils.blocks;

import java.util.concurrent.CompletableFuture;

public abstract class BlockPlacePlan {
    public abstract CompletableFuture<Void> apply();
}