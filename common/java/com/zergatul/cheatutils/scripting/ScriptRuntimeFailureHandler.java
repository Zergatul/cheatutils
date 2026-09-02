package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.LevelUnloadedException;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class ScriptRuntimeFailureHandler {

    public static final ScriptRuntimeFailureHandler instance = new ScriptRuntimeFailureHandler(true);

    private static final int RUNTIME_FAILURE_PRIORITY = Integer.MAX_VALUE;

    private final ConcurrentLinkedQueue<RuntimeException> failures = new ConcurrentLinkedQueue<>();

    ScriptRuntimeFailureHandler() {
        this(false);
    }

    private ScriptRuntimeFailureHandler(boolean registerLifecycleHandler) {
        if (registerLifecycleHandler) {
            Events.ClientTickEnd.add(this::throwPending, RUNTIME_FAILURE_PRIORITY);
        }
    }

    public void report(String message, @Nullable Throwable throwable) {
        RuntimeException failure = createFailure(message, throwable);
        if (failure != null) {
            failures.add(failure);
        }
    }

    public void throwIfFailure(String message, @Nullable Throwable throwable) {
        RuntimeException failure = createFailure(message, throwable);
        if (failure != null) {
            throw failure;
        }
    }

    void throwPending() {
        RuntimeException failure = failures.poll();
        if (failure != null) {
            throw failure;
        }
    }

    private static @Nullable RuntimeException createFailure(String message, @Nullable Throwable throwable) {
        Throwable failure = throwable;
        while ((failure instanceof CompletionException || failure instanceof ExecutionException) && failure.getCause() != null) {
            failure = failure.getCause();
        }
        if (failure == null ||
                failure instanceof CancellationException ||
                failure instanceof LevelUnloadedException ||
                failure instanceof ControllableStopException
        ) {
            return null;
        }
        return new RuntimeException(message, failure);
    }
}