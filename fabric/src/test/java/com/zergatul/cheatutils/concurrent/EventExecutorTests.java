package com.zergatul.cheatutils.concurrent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class EventExecutorTests {

    @Test
    public void shutdownNowDrainsAndCancelsQueuedFutures() {
        TestEventExecutor executor = new TestEventExecutor(10);
        Future<?> first = executor.submit(() -> {});
        Future<?> second = executor.submit(() -> {});

        List<Runnable> queued = executor.shutdownNow();

        Assertions.assertEquals(2, queued.size());
        Assertions.assertTrue(first.isCancelled());
        Assertions.assertTrue(second.isCancelled());
        Assertions.assertTrue(executor.isShutdown());
        Assertions.assertTrue(executor.isTerminated());
        Assertions.assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> {}));
    }

    @Test
    public void shutdownLetsQueuedWorkComplete() {
        TestEventExecutor executor = new TestEventExecutor(10);
        int[] executions = new int[1];
        Future<?> future = executor.submit(() -> executions[0]++);

        executor.shutdown();

        Assertions.assertFalse(executor.isTerminated());
        executor.process();
        Assertions.assertTrue(future.isDone());
        Assertions.assertFalse(future.isCancelled());
        Assertions.assertEquals(1, executions[0]);
        Assertions.assertTrue(executor.isTerminated());
    }

    @Test
    public void fullQueueRejectsAdditionalWork() {
        TestEventExecutor executor = new TestEventExecutor(1);
        executor.execute(() -> {});

        Assertions.assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> {}));

        executor.shutdownNow();
    }

    @Test
    public void clientCloseCancelsQueuedAndDelayedWork() {
        ClientTickEndExecutor executor = new ClientTickEndExecutor();
        CompletableFuture<Void> delay = executor.waitTicks(10);
        Future<?> queued = executor.submit(() -> {});

        executor.onClose();

        Assertions.assertTrue(delay.isCancelled());
        Assertions.assertTrue(queued.isCancelled());
        Assertions.assertTrue(executor.isTerminated());
        Assertions.assertThrows(RejectedExecutionException.class, () -> executor.waitTicks(1));
    }

    @Test
    public void levelUnloadFailsDelaysAndCancelsQueuedWorkWithoutShutdown() {
        InGameTickEndExecutor executor = new InGameTickEndExecutor();
        CompletableFuture<Void> delay = executor.waitTicks(10);
        Future<?> queued = executor.submit(() -> {});

        executor.onLevelUnload();

        Assertions.assertTrue(queued.isCancelled());
        Assertions.assertFalse(executor.isShutdown());
        CompletionException exception = Assertions.assertThrows(CompletionException.class, delay::join);
        Assertions.assertInstanceOf(LevelUnloadedException.class, exception.getCause());
    }

    @Test
    public void inGameCloseCancelsQueuedAndDelayedWork() {
        InGameTickEndExecutor executor = new InGameTickEndExecutor();
        CompletableFuture<Void> delay = executor.waitTicks(10);
        Future<?> queued = executor.submit(() -> {});

        executor.onClose();

        Assertions.assertTrue(delay.isCancelled());
        Assertions.assertTrue(queued.isCancelled());
        Assertions.assertTrue(executor.isTerminated());
        Assertions.assertThrows(RejectedExecutionException.class, () -> executor.waitTicks(1));
    }

    @Test
    public void postedTaskDroppedByShutdownIsCancelled() {
        TestEventExecutor executor = new TestEventExecutor(10);
        int[] executions = new int[1];
        CompletableFuture<Void> posted = ClientThread.post(executor, () -> executions[0]++);

        executor.shutdownNow();
        executor.process();

        Assertions.assertTrue(posted.isCancelled());
        Assertions.assertEquals(0, executions[0]);
    }

    @Test
    public void postedTaskRejectedByExecutorFails() {
        TestEventExecutor executor = new TestEventExecutor(10);
        executor.shutdown();

        CompletableFuture<Void> posted = ClientThread.post(executor, () -> {});

        CompletionException exception = Assertions.assertThrows(CompletionException.class, posted::join);
        Assertions.assertInstanceOf(RejectedExecutionException.class, exception.getCause());
    }

    @Test
    public void postedTaskCompletesWithItsResult() {
        TestEventExecutor executor = new TestEventExecutor(10);
        CompletableFuture<Void> done = ClientThread.post(executor, () -> {});
        CompletableFuture<Void> failed = ClientThread.post(executor, () -> {
            throw new IllegalStateException("broken");
        });

        Assertions.assertFalse(done.isDone());
        executor.process();

        Assertions.assertNull(done.join());
        CompletionException exception = Assertions.assertThrows(CompletionException.class, failed::join);
        Assertions.assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    @Test
    public void postedTaskCancelledBeforeItRunsIsSkipped() {
        TestEventExecutor executor = new TestEventExecutor(10);
        int[] executions = new int[1];
        CompletableFuture<Void> posted = ClientThread.post(executor, () -> executions[0]++);

        posted.cancel(false);
        executor.process();

        Assertions.assertEquals(0, executions[0]);
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
