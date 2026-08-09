package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ScriptExecutionManager {

    public static final ScriptExecutionManager instance = new ScriptExecutionManager();

    private static final int LIFECYCLE_PRIORITY = -100;

    private final Map<ScriptRef, CompletableFuture<?>> executions = new HashMap<>();

    private ScriptExecutionManager() {
        Events.ClientPlayerLoggingOut.add(this::cancelAll, LIFECYCLE_PRIORITY);
        Events.WorldUnload.add(this::cancelAll, LIFECYCLE_PRIORITY);
        Events.DimensionChange.add(this::cancelAll, LIFECYCLE_PRIORITY);
        Events.Close.add(this::cancelAll, LIFECYCLE_PRIORITY);
    }

    public synchronized CompletableFuture<?> execute(ScriptRef ref, AsyncRunnable script) {
        Objects.requireNonNull(ref);
        Objects.requireNonNull(script);

        CompletableFuture<?> current = executions.get(ref);
        if (current != null && !current.isDone()) {
            return current;
        }

        CompletableFuture<?> future = Objects.requireNonNull(script.run(), "Async script returned a null future.");
        track(ref, future);
        return future;
    }

    public synchronized void track(ScriptRef ref, CompletableFuture<?> future) {
        Objects.requireNonNull(ref);
        Objects.requireNonNull(future);

        CompletableFuture<?> previous = executions.put(ref, future);
        if (previous != null && previous != future) {
            previous.cancel(false);
        }

        future.whenComplete((result, throwable) -> removeIfCurrent(ref, future));
    }

    public void cancel(ScriptRef ref) {
        Objects.requireNonNull(ref);

        CompletableFuture<?> future;
        synchronized (this) {
            future = executions.remove(ref);
        }
        if (future != null) {
            future.cancel(false);
        }
    }

    public void cancelAll() {
        ArrayList<CompletableFuture<?>> futures;
        synchronized (this) {
            futures = new ArrayList<>(executions.values());
            executions.clear();
        }
        for (CompletableFuture<?> future : futures) {
            future.cancel(false);
        }
    }

    public synchronized boolean isRunning(ScriptRef ref) {
        CompletableFuture<?> future = executions.get(Objects.requireNonNull(ref));
        return future != null && !future.isDone();
    }

    synchronized int getActiveCount() {
        return executions.size();
    }

    private synchronized void removeIfCurrent(ScriptRef ref, CompletableFuture<?> future) {
        executions.remove(ref, future);
    }
}