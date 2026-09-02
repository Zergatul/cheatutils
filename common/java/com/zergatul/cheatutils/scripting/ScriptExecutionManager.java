package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ScriptExecutionManager {

    public static final ScriptExecutionManager instance = new ScriptExecutionManager(true);

    private static final int LIFECYCLE_PRIORITY = -100;

    private final Map<ScriptRef, CompletableFuture<?>> namedExecutions = new HashMap<>();
    private final Set<CompletableFuture<?>> anonymousExecutions = new HashSet<>();

    ScriptExecutionManager() {
        this(false);
    }

    private ScriptExecutionManager(boolean registerLifecycleHandler) {
        if (registerLifecycleHandler) {
            Events.Close.add(this::cancelAll, LIFECYCLE_PRIORITY);
        }
    }

    public CompletableFuture<?> execute(ScriptRef ref, AsyncRunnable script) {
        return execute(ref, script, null);
    }

    public CompletableFuture<?> execute(
            ScriptRef ref,
            AsyncRunnable script,
            @Nullable Consumer<Throwable> onComplete) {
        Objects.requireNonNull(ref);
        Objects.requireNonNull(script);

        CompletableFuture<?> future;
        synchronized (this) {
            CompletableFuture<?> current = namedExecutions.get(ref);
            if (current != null && !current.isDone()) {
                return current;
            }

            future = Objects.requireNonNull(script.run(), "Async script returned a null future.");
            namedExecutions.put(ref, future);
        }

        future.whenComplete((result, throwable) -> removeIfCurrent(ref, future));
        if (onComplete != null) {
            future.whenComplete((result, throwable) -> onComplete.accept(throwable));
        }
        return future;
    }

    public CompletableFuture<?> execute(AsyncRunnable script) {
        Objects.requireNonNull(script);

        CompletableFuture<?> future = Objects.requireNonNull(script.run(), "Async script returned a null future.");
        track(future);
        return future;
    }

    public void track(ScriptRef ref, CompletableFuture<?> future) {
        Objects.requireNonNull(ref);
        Objects.requireNonNull(future);

        CompletableFuture<?> previous;
        synchronized (this) {
            previous = namedExecutions.put(ref, future);
        }

        future.whenComplete((result, throwable) -> removeIfCurrent(ref, future));

        if (previous != null && previous != future) {
            previous.cancel(false);
        }
    }

    private void track(CompletableFuture<?> future) {
        synchronized (this) {
            anonymousExecutions.add(future);
        }
        future.whenComplete((result, throwable) -> removeAnonymous(future));
    }

    public void cancel(ScriptRef ref) {
        Objects.requireNonNull(ref);

        CompletableFuture<?> future;
        synchronized (this) {
            future = namedExecutions.remove(ref);
        }
        if (future != null) {
            future.cancel(false);
        }
    }

    public void cancelAll() {
        Set<CompletableFuture<?>> futures;
        synchronized (this) {
            futures = new HashSet<>(namedExecutions.values());
            futures.addAll(anonymousExecutions);
            namedExecutions.clear();
            anonymousExecutions.clear();
        }
        for (CompletableFuture<?> future : futures) {
            future.cancel(false);
        }
    }

    public synchronized boolean isRunning(ScriptRef ref) {
        CompletableFuture<?> future = namedExecutions.get(Objects.requireNonNull(ref));
        return future != null && !future.isDone();
    }

    synchronized int getActiveCount() {
        return namedExecutions.size() + anonymousExecutions.size();
    }

    private synchronized void removeIfCurrent(ScriptRef ref, CompletableFuture<?> future) {
        namedExecutions.remove(ref, future);
    }

    private synchronized void removeAnonymous(CompletableFuture<?> future) {
        anonymousExecutions.remove(future);
    }
}