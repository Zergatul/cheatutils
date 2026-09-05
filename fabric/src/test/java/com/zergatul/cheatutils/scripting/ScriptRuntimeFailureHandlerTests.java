package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.concurrent.LevelUnloadedException;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptRuntimeFailureHandlerTests {

    private final ScriptRuntimeFailureHandler handler = new ScriptRuntimeFailureHandler();

    @Test
    public void expectedTerminationIsIgnored() {
        for (Throwable termination : new Throwable[] {
                new CancellationException(), new LevelUnloadedException(), new ControllableStopException() }) {
            assertNull(ScriptRuntimeFailureHandler.unwrapFailure(new CompletionException(new ExecutionException(termination))));
            handler.report("Ignored", termination);
            assertDoesNotThrow(() -> handler.reportSynchronous("Ignored", termination));
        }
        assertNull(ScriptRuntimeFailureHandler.unwrapFailure(null));
        assertDoesNotThrow(handler::throwPendingFatalFailure);
    }

    @Test
    public void ordinaryFailuresAreUnwrappedAndDoNotCrashTheClient() {
        IllegalStateException cause = new IllegalStateException("Original");
        Throwable wrapped = new CompletionException(new ExecutionException(cause));
        assertSame(cause, ScriptRuntimeFailureHandler.unwrapFailure(wrapped));
        assertDoesNotThrow(() -> handler.report("Script failed.", wrapped));
        assertDoesNotThrow(() -> handler.reportSynchronous("Script failed.", wrapped));
        assertDoesNotThrow(handler::throwPendingFatalFailure);
    }

    @Test
    public void structuredRuntimeFailureCreatesConciseNotification() {
        IllegalStateException failure = new IllegalStateException("Something went wrong\non another line");
        handler.report(new ScriptRef(ScriptType.BLOCK_ESP, "minecraft:diamond_ore"), "block rendering", failure);

        ScriptRuntimeFailureHandler.FailureNotification notification = handler.pollNotification();
        assertNotNull(notification);
        assertEquals("Script crashed", notification.title());
        assertTrue(notification.message().contains("Block ESP / minecraft:diamond_ore"));
        assertTrue(notification.message().contains("IllegalStateException: Something went wrong on another line"));
        assertTrue(notification.message().contains("Save the script to re-enable it."));
        assertNull(handler.pollNotification());

        ScriptRuntimeFailureHandler.NotificationHistory history = handler.getNotificationHistory();
        assertEquals(1, history.latestId());
        assertEquals(1, history.notifications().size());
        ScriptRuntimeFailureHandler.ScriptNotification entry = history.notifications().getFirst();
        assertEquals("Script crashed", entry.title());
        assertEquals("Script 'Block ESP / minecraft:diamond_ore' failed during block rendering.", entry.message());
        assertTrue(entry.details().contains("IllegalStateException: Something went wrong"));
    }

    @Test
    public void startupCompilationFailureCreatesOneNotificationForAllDiagnostics() {
        var compilation = new Compiler(ScriptType.KEYBINDING.createParameters()).compile("unknownName(); anotherUnknownName();");
        assertNull(compilation.getProgram());
        assertNotNull(compilation.getDiagnostics());
        assertTrue(compilation.getDiagnostics().size() >= 2);
        handler.reportCompilationFailure(
                new ScriptRef(ScriptType.KEYBINDING, "Broken"),
                compilation.getDiagnostics());

        ScriptRuntimeFailureHandler.FailureNotification notification = handler.pollNotification();
        assertNotNull(notification);
        assertEquals("Script compilation failed", notification.title());
        assertTrue(notification.message().contains("Key Bindings / Broken"));
        assertTrue(notification.message().contains(compilation.getDiagnostics().getFirst().message));
        assertFalse(notification.message().contains(compilation.getDiagnostics().get(1).message));
        assertNull(handler.pollNotification());

        ScriptRuntimeFailureHandler.ScriptNotification entry = handler.getNotificationHistory().notifications().getFirst();
        assertEquals("Script compilation failed", entry.title());
        assertTrue(entry.details().contains(compilation.getDiagnostics().getFirst().message));
        assertTrue(entry.details().contains(compilation.getDiagnostics().get(1).message));
    }

    @Test
    public void notificationHistoryKeepsOnlyTheNewestHundredEntries() {
        for (int i = 1; i <= 105; i++) {
            handler.addToHistory("Title " + i, "Message " + i, "Details " + i);
        }

        ScriptRuntimeFailureHandler.NotificationHistory history = handler.getNotificationHistory();
        assertEquals(105, history.latestId());
        assertEquals(100, history.notifications().size());
        assertEquals(105, history.notifications().getFirst().id());
        assertEquals(6, history.notifications().getLast().id());
        assertThrows(UnsupportedOperationException.class, () -> history.notifications().clear());
    }

    @Test
    public void fatalSynchronousErrorsEscapeAndAsyncErrorsReachTheGameLoop() {
        OutOfMemoryError error = new OutOfMemoryError("Test only");
        assertSame(error, assertThrows(OutOfMemoryError.class, () -> handler.reportSynchronous("Fatal", error)));
        handler.report("Fatal", new CompletionException(error));
        assertSame(error, assertThrows(OutOfMemoryError.class, handler::throwPendingFatalFailure));
        assertDoesNotThrow(handler::throwPendingFatalFailure);
    }
}
