package com.zergatul.cheatutils.concurrent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class EventExecutorSmokeTest {

    private EventExecutorSmokeTest() {}

    public static void run() {
        verifyImmediateShutdown();
        verifyGracefulShutdown();
        verifyClientTickShutdown();
        verifyInGameWorldUnloadAndShutdown();
    }

    private static void verifyImmediateShutdown() {
        TestEventExecutor executor = new TestEventExecutor(10);
        Future<?> first = executor.submit(() -> {});
        Future<?> second = executor.submit(() -> {});

        List<Runnable> queued = executor.shutdownNow();
        if (queued.size() != 2 || !first.isCancelled() || !second.isCancelled()) {
            throw new IllegalStateException("Immediate event-executor shutdown did not cancel all queued futures.");
        }
        if (!executor.isShutdown() || !executor.isTerminated()) {
            throw new IllegalStateException("Drained event executor did not terminate.");
        }

        requireRejected(() -> executor.execute(() -> {}));

        TestEventExecutor fullExecutor = new TestEventExecutor(1);
        fullExecutor.execute(() -> {});
        requireRejected(() -> fullExecutor.execute(() -> {}));
        fullExecutor.shutdownNow();
    }

    private static void verifyGracefulShutdown() {
        TestEventExecutor executor = new TestEventExecutor(10);
        int[] executions = new int[1];
        Future<?> future = executor.submit(() -> executions[0]++);

        executor.shutdown();
        if (executor.isTerminated()) {
            throw new IllegalStateException("Graceful event-executor shutdown discarded queued work.");
        }
        executor.process();
        if (!future.isDone() || future.isCancelled() || executions[0] != 1 || !executor.isTerminated()) {
            throw new IllegalStateException("Graceful event-executor shutdown did not finish queued work.");
        }
    }

    private static void verifyClientTickShutdown() {
        ClientTickEndExecutor executor = new ClientTickEndExecutor();
        CompletableFuture<Void> delay = executor.waitTicks(10);
        Future<?> queued = executor.submit(() -> {});

        executor.onClose();
        if (!delay.isCancelled() || !queued.isCancelled() || !executor.isTerminated()) {
            throw new IllegalStateException("Client-tick executor retained work after client close.");
        }
        requireRejected(() -> executor.waitTicks(1));
    }

    private static void verifyInGameWorldUnloadAndShutdown() {
        InGameTickEndExecutor executor = new InGameTickEndExecutor();
        CompletableFuture<Void> worldDelay = executor.waitTicks(10);
        Future<?> worldTask = executor.submit(() -> {});

        executor.onWorldUnload();
        if (!worldTask.isCancelled() || executor.isShutdown()) {
            throw new IllegalStateException("World unload did not clear queued in-game work correctly.");
        }
        try {
            worldDelay.join();
            throw new IllegalStateException("World unload did not fail an in-game tick delay.");
        }
        catch (CompletionException e) {
            if (!(e.getCause() instanceof LevelUnloadedException)) {
                throw e;
            }
        }

        CompletableFuture<Void> closeDelay = executor.waitTicks(10);
        Future<?> closeTask = executor.submit(() -> {});
        executor.onClose();
        if (!closeDelay.isCancelled() || !closeTask.isCancelled() || !executor.isTerminated()) {
            throw new IllegalStateException("In-game executor retained work after client close.");
        }
        requireRejected(() -> executor.waitTicks(1));
    }

    private static void requireRejected(Runnable runnable) {
        try {
            runnable.run();
            throw new IllegalStateException("Event executor accepted work after shutdown.");
        }
        catch (RejectedExecutionException ignored) {
        }
    }

    private static class TestEventExecutor extends EventExecutor {

        private TestEventExecutor(int capacity) {
            super(capacity);
        }

        private void process() {
            processQueue();
        }
    }
}
