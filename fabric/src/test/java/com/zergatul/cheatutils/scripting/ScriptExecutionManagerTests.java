package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class ScriptExecutionManagerTests {

    private final ScriptExecutionManager manager = new ScriptExecutionManager();

    @AfterEach
    public void cancelExecutions() {
        manager.cancelAll();
    }

    @Test
    public void namedExecutionSuppressesConcurrentStart() {
        ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, "test");
        int[] starts = new int[1];
        AsyncRunnable script = () -> {
            starts[0]++;
            return new CompletableFuture<>();
        };

        CompletableFuture<?> first = manager.execute(ref, script);
        CompletableFuture<?> second = manager.execute(ref, script);

        Assertions.assertSame(first, second);
        Assertions.assertEquals(1, starts[0]);
        Assertions.assertTrue(manager.isRunning(ref));
        Assertions.assertEquals(1, manager.getActiveCount());
    }

    @Test
    public void namedCompletionHandlerIsAttachedOnlyByStartingCall() {
        ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, "test");
        CompletableFuture<Void> future = new CompletableFuture<>();
        int[] completions = new int[1];

        manager.execute(ref, () -> future, throwable -> completions[0]++);
        manager.execute(ref, () -> future, throwable -> completions[0]++);
        future.complete(null);

        Assertions.assertEquals(1, completions[0]);
    }

    @Test
    public void completedNamedExecutionIsRemovedAndCanRestart() {
        ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, "test");
        CompletableFuture<Void> first = new CompletableFuture<>();

        manager.track(ref, first);
        first.complete(null);

        Assertions.assertFalse(manager.isRunning(ref));
        Assertions.assertEquals(0, manager.getActiveCount());

        CompletableFuture<Void> second = new CompletableFuture<>();
        Assertions.assertSame(second, manager.execute(ref, () -> second));
        Assertions.assertTrue(manager.isRunning(ref));
    }

    @Test
    public void replacingNamedExecutionCancelsPreviousOutsideManagerLock() {
        ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, "test");
        CompletableFuture<Void> first = new CompletableFuture<>();
        CompletableFuture<Void> second = new CompletableFuture<>();
        boolean[] callbackCouldEnterManager = new boolean[1];
        first.whenComplete((result, throwable) -> callbackCouldEnterManager[0] = Thread.holdsLock(manager));

        manager.track(ref, first);
        manager.track(ref, second);

        Assertions.assertTrue(first.isCancelled());
        Assertions.assertFalse(callbackCouldEnterManager[0]);
        Assertions.assertTrue(manager.isRunning(ref));
        Assertions.assertEquals(1, manager.getActiveCount());
    }

    @Test
    public void anonymousExecutionsDoNotSuppressEachOther() {
        CompletableFuture<Void> first = new CompletableFuture<>();
        CompletableFuture<Void> second = new CompletableFuture<>();

        Assertions.assertSame(first, manager.execute(() -> first));
        Assertions.assertSame(second, manager.execute(() -> second));
        Assertions.assertEquals(2, manager.getActiveCount());

        first.complete(null);
        Assertions.assertEquals(1, manager.getActiveCount());
        Assertions.assertFalse(second.isDone());
    }

    @Test
    public void cancellationCanTargetNamedExecution() {
        ScriptRef firstRef = new ScriptRef(ScriptType.KEYBINDING, "first");
        ScriptRef secondRef = new ScriptRef(ScriptType.KEYBINDING, "second");
        CompletableFuture<Void> first = new CompletableFuture<>();
        CompletableFuture<Void> second = new CompletableFuture<>();
        manager.track(firstRef, first);
        manager.track(secondRef, second);

        manager.cancel(firstRef);

        Assertions.assertTrue(first.isCancelled());
        Assertions.assertFalse(second.isDone());
        Assertions.assertFalse(manager.isRunning(firstRef));
        Assertions.assertTrue(manager.isRunning(secondRef));
    }

    @Test
    public void cancelAllIncludesNamedAndAnonymousExecutions() {
        CompletableFuture<Void> named = new CompletableFuture<>();
        CompletableFuture<Void> anonymous = new CompletableFuture<>();
        manager.track(new ScriptRef(ScriptType.KEYBINDING, "test"), named);
        manager.execute(() -> anonymous);

        manager.cancelAll();

        Assertions.assertTrue(named.isCancelled());
        Assertions.assertTrue(anonymous.isCancelled());
        Assertions.assertEquals(0, manager.getActiveCount());
    }

    @Test
    public void nullFutureIsRejectedWithoutBeingTracked() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> manager.execute(() -> null));

        Assertions.assertEquals("Async script returned a null future.", exception.getMessage());
        Assertions.assertEquals(0, manager.getActiveCount());
    }
}
