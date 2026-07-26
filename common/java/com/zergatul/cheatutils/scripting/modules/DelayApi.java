package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.concurrent.InGameTickEndExecutor;
import com.zergatul.scripting.MethodDescription;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class DelayApi {

    @MethodDescription("""
            Stops script execution for specified amount of ticks.
            Continuation will not run if you disconnect from the world.
            """)
    public CompletableFuture<Void> ticks(int ticks) {
        if (ticks <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        return InGameTickEndExecutor.instance.waitTicks(ticks);
    }

    @MethodDescription("""
            Stops script execution for specified amount of ticks.
            Continuation may run after you disconnected from the world.
            """)
    public CompletableFuture<Void> clientTicks(int ticks) {
        if (ticks <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        return ClientTickEndExecutor.instance.waitTicks(ticks);
    }
}