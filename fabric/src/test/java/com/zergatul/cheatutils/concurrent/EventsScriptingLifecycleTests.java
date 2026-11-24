package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.modules.EventsApi;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.InstanceCompilationResult;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class EventsScriptingLifecycleTests {

    private final EventsApi events = new EventsApi();

    @BeforeAll
    public static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    public void clear() throws Exception {
        clearScript();
        drainFailures();
    }

    @Test
    public void replacementDeactivatesOldScriptBeforeUnloadAndUnloadsEachInstallationOnce() throws Exception {
        List<String> calls = new ArrayList<>();
        AtomicReference<ScriptActivation<?>> old = new AtomicReference<>();
        AtomicBoolean activeDuringUnload = new AtomicBoolean(true);
        AtomicBoolean callbackRan = new AtomicBoolean();
        install(() -> {
            calls.add("old initialization");
            events.onUnload(() -> {
                calls.add("old unload");
                activeDuringUnload.set(old.get().isActive());
                old.get().run("callback during unload", () -> callbackRan.set(true));
            });
        });
        old.set(getActivation());
        assertEquals(List.of("old initialization"), calls);

        install(() -> {
            calls.add("new initialization");
            events.onUnload(() -> calls.add("new unload"));
        });

        assertEquals(List.of("old initialization", "old unload", "new initialization"), calls);
        assertFalse(activeDuringUnload.get());
        assertFalse(callbackRan.get());
        assertNotSame(old.get(), getActivation());
        assertTrue(getActivation().isActive());

        // Removing a script and clearing it again must not repeat either installation's cleanup.
        install(null);
        clearScript();
        assertEquals(List.of("old initialization", "old unload", "new initialization", "new unload"), calls);
        assertNull(getActivation());
    }

    @Test
    public void callbackFailureRemovesExternalHandlerAndDoesNotUnloadAgainOnClear() throws Exception {
        List<Runnable> externalHandlers = new ArrayList<>();
        AtomicInteger unloads = new AtomicInteger();
        Runnable handler = () -> {};
        install(() -> {
            externalHandlers.add(handler);
            events.onUnload(() -> {
                externalHandlers.remove(handler);
                unloads.incrementAndGet();
            });
        });
        assertEquals(List.of(handler), externalHandlers);

        assertFalse(getActivation().run("tick", () -> { throw new IllegalStateException("callback failed"); }));
        assertFalse(getActivation().isActive());
        assertEquals(0, unloads.get());
        drainFailures();

        assertTrue(externalHandlers.isEmpty());
        assertEquals(1, unloads.get());
        drainFailures();
        clearScript();
        assertEquals(1, unloads.get());
    }

    @Test
    public void initializationFailureUnloadsPartiallyRegisteredResourcesOnce() throws Exception {
        List<Runnable> externalHandlers = new ArrayList<>();
        AtomicInteger unloads = new AtomicInteger();
        Runnable handler = () -> {};
        install(() -> {
            externalHandlers.add(handler);
            events.onUnload(() -> {
                externalHandlers.remove(handler);
                unloads.incrementAndGet();
            });
            throw new IllegalStateException("initialization failed");
        });

        assertFalse(getActivation().isActive());
        assertEquals(List.of(handler), externalHandlers);
        drainFailures();

        assertTrue(externalHandlers.isEmpty());
        assertEquals(1, unloads.get());
        clearScript();
        drainFailures();
        assertEquals(1, unloads.get());
    }

    @Test
    public void failingUnloadDoesNotPreventRemainingHandlersOrReplacement() throws Exception {
        List<String> calls = new ArrayList<>();
        install(() -> {
            events.onUnload(() -> {
                calls.add("first unload");
                throw new IllegalStateException("cleanup failed");
            });
            events.onUnload(() -> calls.add("second unload"));
        });

        install(() -> calls.add("replacement initialization"));

        assertEquals(List.of("first unload", "second unload", "replacement initialization"), calls);
        assertTrue(getActivation().isActive());
        drainFailures();
        clearScript();
        assertEquals(List.of("first unload", "second unload", "replacement initialization"), calls);
    }

    @Test
    public void pendingFailureDoesNotUnloadReplacementOrRepeatOldCleanup() throws Exception {
        List<String> calls = new ArrayList<>();
        install(() -> events.onUnload(() -> calls.add("old unload")));
        assertFalse(getActivation().run("tick", () -> { throw new IllegalStateException("callback failed"); }));

        // Replace before the next tick drains the old installation's pending failure.
        install(() -> events.onUnload(() -> calls.add("new unload")));
        assertEquals(List.of("old unload"), calls);
        drainFailures();

        assertEquals(List.of("old unload"), calls);
        assertTrue(getActivation().isActive());
        clearScript();
        assertEquals(List.of("old unload", "new unload"), calls);
    }

    @Test
    public void replacementDuringDispatchStopsTheOldCallbackLoop() throws Exception {
        List<String> calls = new ArrayList<>();
        install(() -> {
            events.onTickEnd(() -> {
                calls.add("first");
                // a callback replacing its own script, the way a profile load on the client thread does
                install(() -> events.onTickEnd(() -> calls.add("replacement")));
            });
            events.onTickEnd(() -> calls.add("second"));
        });
        ScriptActivation<?> old = getActivation();
        List<?> oldCallbacks = EventsScriptingAccess.getCallbacks("onTickEnd");

        // the same loop shape as the dispatchers: iterate one installation, stop when its activation is retired
        for (Object callback : oldCallbacks) {
            if (!old.run("game tick", (Runnable) callback)) break;
        }

        assertEquals(List.of("first"), calls);
        assertNotSame(old, getActivation());
        assertEquals(1, EventsScriptingAccess.getCallbacks("onTickEnd").size());
    }

    @Test
    public void callbackOfReplacedScriptCannotRegisterIntoReplacement() throws Exception {
        // Scripts using the API need the game, but any object of a compiled class belongs to its script.
        // The programs themselves stand in for callbacks compiled as part of them.
        Runnable old = compile("int x = 1;").getProgram();
        Runnable replacement = compile("int x = 2;").getProgram();

        install(old);
        EventsScripting.INSTANCE.addOnTickEnd(old);
        assertEquals(1, EventsScriptingAccess.getCallbacks("onTickEnd").size());

        install(replacement);
        // e.g. a callback of the old script that was already running on the Netty thread
        EventsScripting.INSTANCE.addOnTickEnd(old);
        assertTrue(EventsScriptingAccess.getCallbacks("onTickEnd").isEmpty());

        EventsScripting.INSTANCE.addOnTickEnd(replacement);
        assertEquals(1, EventsScriptingAccess.getCallbacks("onTickEnd").size());
    }

    @Test
    public void callbackRegistrationsAfterFailureAreIgnored() throws Exception {
        install(() -> {});
        assertFalse(getActivation().run("tick", () -> { throw new IllegalStateException("callback failed"); }));

        events.onTickEnd(() -> {});

        assertTrue(EventsScriptingAccess.getCallbacks("onTickEnd").isEmpty());
    }

    private static InstanceCompilationResult compile(String code) {
        InstanceCompilationResult result = new Compiler(ScriptType.EVENTS.createParameters()).compileInstance(code);
        assertNotNull(result.getProgram(), () -> String.valueOf(result.getDiagnostics()));
        return result;
    }

    private static void install(Runnable program) {
        EventsScripting.INSTANCE.setScript(program);
    }

    private static void clearScript() {
        EventsScripting.INSTANCE.clear();
    }

    private static ScriptActivation<?> getActivation() throws Exception {
        return EventsScriptingAccess.getActivation();
    }

    private static void drainFailures() throws Exception {
        EventsScriptingAccess.drainFailures();
    }
}