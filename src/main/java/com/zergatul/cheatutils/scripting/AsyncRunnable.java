package com.zergatul.cheatutils.scripting;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface AsyncRunnable {
    CompletableFuture<?> run();
}