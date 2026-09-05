package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.scripting.compiler.DynamicCompilerClassLoader;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Runtime-only state belonging to one installed program, never to its saved document. */
public final class ScriptActivation<T> {

    // Each compilation has its own loader, shared by its program and generated closures.
    // Unlike thread-local ownership, this also works after an async script resumes from await.
    private static final Map<ClassLoader, WeakReference<ScriptActivation<?>>> owners = Collections.synchronizedMap(new WeakHashMap<>());

    public final T program;
    private final ScriptRef ref;
    private final AtomicBoolean failed = new AtomicBoolean();
    private final AtomicReference<CompletableFuture<?>> execution = new AtomicReference<>();
    private final Consumer<ScriptActivation<T>> onFailure;
    private final FailureReporter reporter;

    public ScriptActivation(ScriptRef ref, T program) {
        this(ref, program, _ -> {});
    }

    public ScriptActivation(ScriptRef ref, T program, Consumer<ScriptActivation<T>> onFailure) {
        this(ref, program, onFailure, ScriptRuntimeFailureHandler.instance::report);
    }

    ScriptActivation(ScriptRef ref, T program, Consumer<ScriptActivation<T>> onFailure, FailureReporter reporter) {
        this.ref = Objects.requireNonNull(ref);
        this.program = Objects.requireNonNull(program);
        this.onFailure = Objects.requireNonNull(onFailure);
        this.reporter = Objects.requireNonNull(reporter);
        ClassLoader loader = program.getClass().getClassLoader();
        if (loader instanceof DynamicCompilerClassLoader) {
            owners.put(loader, new WeakReference<>(this));
        }
    }

    public static @Nullable ScriptActivation<?> findOwner(Object callback) {
        WeakReference<ScriptActivation<?>> owner = owners.get(callback.getClass().getClassLoader());
        return owner == null ? null : owner.get();
    }

    public boolean isActive() {
        return !failed.get();
    }

    /** Retires an installation and cancels its pending async continuation, without reporting a crash. */
    public void deactivate() {
        failed.set(true);
        cancelExecution();
    }

    public boolean run(String context, Runnable action) {
        if (!isActive()) {
            return false;
        }
        try {
            action.run();
            return isActive();
        } catch (Throwable throwable) {
            fail(context, throwable);
            return false;
        }
    }

    public boolean test(String context, BooleanSupplier predicate) {
        if (!isActive()) {
            return false;
        }
        try {
            return predicate.getAsBoolean() && isActive();
        } catch (Throwable throwable) {
            fail(context, throwable);
            return false;
        }
    }

    /** Returns the original future so cancellation still reaches the generated state machine. */
    public CompletableFuture<?> execute(AsyncRunnable action) {
        if (!isActive()) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<?> future;
        try {
            future = Objects.requireNonNull(action.run(), "Async script returned a null future.");
        } catch (Throwable throwable) {
            fail("execution", throwable);
            return CompletableFuture.failedFuture(throwable);
        }
        execution.set(future);
        future.whenComplete((_, throwable) -> {
            execution.compareAndSet(future, null);
            Throwable failure = ScriptRuntimeFailureHandler.unwrapFailure(throwable);
            if (ScriptRuntimeFailureHandler.isFatal(failure)) {
                reporter.report(ref, "execution", failure);
            } else {
                fail("execution", failure);
            }
        });
        if (!isActive()) {
            future.cancel(false);
        }
        return future;
    }

    private void fail(String context, @Nullable Throwable throwable) {
        Throwable failure = ScriptRuntimeFailureHandler.unwrapFailure(throwable);
        if (failure == null) {
            return;
        }
        ScriptRuntimeFailureHandler.rethrowIfFatal(failure);
        if (failed.compareAndSet(false, true)) {
            cancelExecution();
            reporter.report(ref, context, failure);
            onFailure.accept(this);
        }
    }

    private void cancelExecution() {
        CompletableFuture<?> future = execution.getAndSet(null);
        if (future != null) {
            future.cancel(false);
        }
    }

    @FunctionalInterface
    interface FailureReporter {
        void report(ScriptRef ref, String context, Throwable failure);
    }
}