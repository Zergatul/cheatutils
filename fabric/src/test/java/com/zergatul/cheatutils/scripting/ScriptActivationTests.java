package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.concurrent.LevelUnloadedException;
import com.zergatul.cheatutils.concurrent.InGameTickEndExecutor;
import com.zergatul.cheatutils.scripting.modules.DelayedApi;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.type.SVoidType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptActivationTests {

    private final List<String> messages = new ArrayList<>();
    private final List<ScriptRef> refs = new ArrayList<>();
    private final List<Throwable> failures = new ArrayList<>();

    private <T> ScriptActivation<T> activation(T program) {
        return new ScriptActivation<>(new ScriptRef(ScriptType.KEYBINDING, "Example"), program, _ -> {}, (ref, context, failure) -> {
            refs.add(ref);
            messages.add(context);
            failures.add(failure);
        });
    }

    @Test
    public void synchronousFailureStopsOnlyThisInstallationAndReportsOnce() {
        AtomicInteger calls = new AtomicInteger();
        Runnable program = () -> { calls.incrementAndGet(); throw new IllegalStateException("broken"); };
        var old = activation(program);
        assertFalse(old.run("callback", old.program));
        assertFalse(old.run("callback", old.program));
        assertEquals(1, calls.get());
        assertEquals(1, failures.size());
        assertEquals(new ScriptRef(ScriptType.KEYBINDING, "Example"), refs.getFirst());
        assertEquals("callback", messages.getFirst());

        var savedAgain = activation(program);
        assertTrue(savedAgain.isActive());
        assertFalse(savedAgain.run("callback", savedAgain.program));
        assertEquals(2, calls.get());
        assertEquals(2, failures.size());
    }

    @Test
    public void predicatesFailClosedAndShareFailureStateWithOtherCallbacks() {
        var script = activation(new Object());
        AtomicInteger skippedCalls = new AtomicInteger();
        assertTrue(script.test("filter", () -> true));
        assertFalse(script.test("filter", () -> { throw new IllegalArgumentException(); }));
        assertFalse(script.test("another filter", () -> skippedCalls.incrementAndGet() > 0));
        assertFalse(script.run("tick", skippedCalls::incrementAndGet));
        assertEquals(0, skippedCalls.get());
        assertEquals(1, failures.size());
    }

    @Test
    public void asyncFailureRetainsOriginalFutureAndCannotDisableReplacement() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        AsyncRunnable program = () -> future;
        var old = activation(program);
        assertSame(future, old.execute(old.program));
        var replacement = activation(program);
        future.completeExceptionally(new IllegalStateException("late failure"));
        assertFalse(old.isActive());
        assertTrue(replacement.isActive());
        assertTrue(old.execute(() -> { fail("Must not restart"); return null; }).isDone());
        assertEquals(1, failures.size());
    }

    @Test
    public void cancellationStillReachesTheOriginalFutureAndDoesNotDisableScript() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        var script = activation((AsyncRunnable) () -> future);
        script.execute(script.program).cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(script.isActive());
        assertTrue(failures.isEmpty());
    }

    @Test
    public void synchronousAsyncFailureAndNullFutureDisableScript() {
        var throwing = activation((AsyncRunnable) () -> { throw new IllegalStateException(); });
        assertTrue(throwing.execute(throwing.program).isCompletedExceptionally());
        assertFalse(throwing.isActive());
        var returningNull = activation((AsyncRunnable) () -> null);
        assertTrue(returningNull.execute(returningNull.program).isCompletedExceptionally());
        assertFalse(returningNull.isActive());
        assertEquals(2, failures.size());
    }

    @Test
    public void expectedTerminationDoesNotDisableScript() {
        for (Throwable termination : new Throwable[] { new CancellationException(), new LevelUnloadedException(), new ControllableStopException() }) {
            var script = activation(new Object());
            assertFalse(script.run("callback", () -> { throw new CompletionException(termination); }));
            assertTrue(script.isActive());
            script.execute(() -> CompletableFuture.failedFuture(termination));
            assertTrue(script.isActive());
        }
        assertTrue(failures.isEmpty());
    }

    @Test
    public void concurrentFailuresReportAndCleanUpOnlyOnce() {
        AtomicInteger reports = new AtomicInteger();
        AtomicInteger cleanups = new AtomicInteger();
        var script = new ScriptActivation<>(new ScriptRef(ScriptType.EVENTS), new Object(),
                _ -> cleanups.incrementAndGet(), (_, _, _) -> reports.incrementAndGet());
        IntStream.range(0, 100).parallel().forEach(_ -> script.run("callback", () -> { throw new IllegalStateException(); }));
        assertEquals(1, reports.get());
        assertEquals(1, cleanups.get());
    }

    @Test
    public void retirementStopsOldCallbacksWithoutReportingFailure() {
        var script = activation(new Object());
        AtomicInteger calls = new AtomicInteger();
        script.deactivate();
        assertFalse(script.run("callback", calls::incrementAndGet));
        assertFalse(script.test("filter", () -> true));
        assertEquals(0, calls.get());
        assertTrue(failures.isEmpty());
    }

    @Test
    public void callbackFailureAndRetirementCancelPendingAsyncExecution() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        var script = activation((AsyncRunnable) () -> future);
        script.execute(script.program);
        script.run("delayed callback", () -> { throw new IllegalStateException(); });
        assertTrue(future.isCancelled());
        assertFalse(script.isActive());
        assertEquals(1, failures.size());

        CompletableFuture<Void> another = new CompletableFuture<>();
        var retired = activation((AsyncRunnable) () -> another);
        retired.execute(retired.program);
        retired.deactivate();
        assertTrue(another.isCancelled());
        assertEquals(1, failures.size());
    }

    @Test
    public void generatedClosuresRetainTheirOwnerAfterAwait() {
        CaptureRoot.capture = new CaptureApi();
        var parameters = new CompilationParametersBuilder()
                .setRoot(CaptureRoot.class)
                .setInterface(AsyncRunnable.class)
                .setAsyncReturnType(SVoidType.instance)
                .build();
        var result = new Compiler(parameters).compile("await capture.waitForSignal(); capture.store(() => {});");
        assertNotNull(result.getProgram(), () -> String.valueOf(result.getDiagnostics()));
        var script = activation(result.<AsyncRunnable>getProgram());
        CompletableFuture<?> future = script.execute(script.program);
        assertNull(CaptureRoot.capture.callback);
        CaptureRoot.capture.signal.complete(null);
        assertFalse(future.isCompletedExceptionally());
        assertTrue(future.isDone());
        assertSame(script, ScriptActivation.findOwner(CaptureRoot.capture.callback));

        var otherResult = new Compiler(parameters).compile("capture.store(() => {});");
        assertNotNull(otherResult.getProgram());
        var other = activation(otherResult.<AsyncRunnable>getProgram());
        other.execute(other.program);
        assertSame(other, ScriptActivation.findOwner(CaptureRoot.capture.callback));
    }

    @Test
    public void delayedCallbackFailureStopsOtherCallbacksAndRetirementSkipsThem() throws Exception {
        CaptureRoot.capture = new CaptureApi();
        var parameters = new CompilationParametersBuilder().setRoot(CaptureRoot.class).build();
        var result = new Compiler(parameters).compile("capture.store(() => capture.crash());");
        assertNotNull(result.getProgram(), () -> String.valueOf(result.getDiagnostics()));
        var script = activation(result.<Runnable>getProgram());
        assertTrue(script.run("initialization", script.program));
        DelayedApi delayed = new DelayedApi();
        delayed.run(1, CaptureRoot.capture.callback);
        delayed.run(1, CaptureRoot.capture.callback);
        advanceDelayedTick();
        assertFalse(script.isActive());
        assertEquals(1, CaptureRoot.capture.calls);
        assertEquals(1, failures.size());

        var saved = activation(result.<Runnable>getProgram());
        assertTrue(saved.run("initialization", saved.program));
        delayed.run(1, CaptureRoot.capture.callback);
        saved.deactivate();
        advanceDelayedTick();
        assertEquals(1, CaptureRoot.capture.calls);
        assertEquals(1, failures.size());
    }

    private static void advanceDelayedTick() throws Exception {
        for (String name : List.of("onTickStart", "onTickEnd")) {
            var method = InGameTickEndExecutor.class.getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(InGameTickEndExecutor.instance);
        }
    }

    public static class CaptureRoot {
        public static CaptureApi capture;
    }

    public static class CaptureApi {
        public final CompletableFuture<Void> signal = new CompletableFuture<>();
        public Runnable callback;
        public int calls;

        public CompletableFuture<Void> waitForSignal() {
            return signal;
        }

        public void store(Runnable callback) {
            this.callback = callback;
        }

        public void crash() {
            calls++;
            throw new IllegalStateException("delayed failure");
        }
    }

    @Test
    public void fatalErrorsAreNotConvertedToOrdinaryScriptFailures() {
        var script = activation(new Object());
        assertThrows(StackOverflowError.class, () -> script.run("callback", () -> { throw new StackOverflowError(); }));
        assertTrue(script.isActive());
        LinkageError error = new LinkageError("Test only");
        script.execute(() -> CompletableFuture.failedFuture(error));
        assertSame(error, failures.getFirst()); // Routed to the handler for rethrow on the client thread.
        assertTrue(script.isActive());
    }
}
