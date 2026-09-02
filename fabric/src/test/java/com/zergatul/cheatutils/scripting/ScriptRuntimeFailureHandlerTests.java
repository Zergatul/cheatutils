package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.concurrent.LevelUnloadedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class ScriptRuntimeFailureHandlerTests {

    private final ScriptRuntimeFailureHandler handler = new ScriptRuntimeFailureHandler();

    @Test
    public void cancellationIsIgnored() {
        handler.report("Ignored", new CompletionException(new CancellationException()));
        Assertions.assertDoesNotThrow(handler::throwPending);
        Assertions.assertDoesNotThrow(() -> handler.throwIfFailure("Ignored", new CancellationException()));
    }

    @Test
    public void lifecycleAndControlFlowTerminationIsIgnored() {
        handler.report("Ignored", new CompletionException(new LevelUnloadedException()));
        handler.report("Ignored", new CompletionException(new ControllableStopException()));

        Assertions.assertDoesNotThrow(handler::throwPending);
        Assertions.assertDoesNotThrow(
                () -> handler.throwIfFailure("Ignored", new CompletionException(new LevelUnloadedException())));
        Assertions.assertDoesNotThrow(
                () -> handler.throwIfFailure("Ignored", new CompletionException(new ControllableStopException())));
    }

    @Test
    public void asynchronousFailureIsQueuedAndUnwrapped() {
        IllegalStateException cause = new IllegalStateException("Original");
        handler.report("Script failed.", new CompletionException(new ExecutionException(cause)));

        RuntimeException failure = Assertions.assertThrows(RuntimeException.class, handler::throwPending);

        Assertions.assertEquals("Script failed.", failure.getMessage());
        Assertions.assertSame(cause, failure.getCause());
        Assertions.assertDoesNotThrow(handler::throwPending);
    }

    @Test
    public void multipleFailuresAreNotLost() {
        handler.report("First", new IllegalStateException());
        handler.report("Second", new IllegalArgumentException());

        Assertions.assertEquals("First", Assertions.assertThrows(RuntimeException.class, handler::throwPending).getMessage());
        Assertions.assertEquals("Second", Assertions.assertThrows(RuntimeException.class, handler::throwPending).getMessage());
        Assertions.assertDoesNotThrow(handler::throwPending);
    }

    @Test
    public void synchronousFailureIsThrownImmediately() {
        IllegalStateException cause = new IllegalStateException("Original");

        RuntimeException failure = Assertions.assertThrows(
                RuntimeException.class,
                () -> handler.throwIfFailure("Script failed.", cause));

        Assertions.assertEquals("Script failed.", failure.getMessage());
        Assertions.assertSame(cause, failure.getCause());
    }
}
