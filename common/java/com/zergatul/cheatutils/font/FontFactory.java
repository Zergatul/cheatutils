package com.zergatul.cheatutils.font;

import java.util.concurrent.CompletableFuture;

public abstract class FontFactory {
    public abstract CompletableFuture<FontReference> create(String name);
}