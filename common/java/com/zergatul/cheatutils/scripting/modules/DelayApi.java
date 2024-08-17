package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.utilities.DelayedRun;
import com.zergatul.scripting.MethodDescription;

import java.util.concurrent.CompletableFuture;

public class DelayApi {

    @MethodDescription("""
            Stops script execution for specified amount of ticks
            """)
    public CompletableFuture<Void> ticks(int ticks) {
        if (ticks <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        DelayedRun.instance.add(ticks, () -> future.complete(null));
        return future;
    }
}