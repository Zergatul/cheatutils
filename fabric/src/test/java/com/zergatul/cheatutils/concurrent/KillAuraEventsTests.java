package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.modules.hacks.KillAura;
import com.zergatul.cheatutils.modules.automation.AimAssist;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.modules.EventsApi;
import com.zergatul.scripting.compiler.Compiler;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class KillAuraEventsTests {

    @BeforeAll
    public static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    public void clear() throws Exception {
        EventsScripting.INSTANCE.clear();
        ClientTickEndExecutor.instance.processQueue();
        drainFailures();
    }

    @Test
    public void documentedExampleCompilesAsEventsScript() {
        var result = new Compiler(ScriptType.EVENTS.createParameters()).compile("""
                events.modules.killAura.addTargetFilter(id => {
                    return game.entities.getName(id) != "your_friend";
                });
                """);
        assertNotNull(result.getProgram(), () -> String.valueOf(result.getDiagnostics()));
    }

    @Test
    public void killAuraOnlyRegistrationsCombineAndShortCircuit() throws Exception {
        EventsApi events = new EventsApi();
        List<Integer> calls = new ArrayList<>();
        EventsScripting.INSTANCE.setScript(() -> {
            events.modules.killAura.addTargetFilter(id -> {
                calls.add(1);
                return id != 10;
            });
            events.modules.killAura.addTargetFilter(id -> {
                calls.add(2);
                return id == 20;
            });
        });
        ClientTickEndExecutor.instance.processQueue();

        KillAura.TargetPredicate predicate = getPredicate();
        assertNotNull(predicate);
        assertFalse(predicate.test(10));
        assertEquals(List.of(1), calls);
        calls.clear();
        assertFalse(predicate.test(30));
        assertEquals(List.of(1, 2), calls);
        calls.clear();
        assertTrue(predicate.test(20));
        assertEquals(List.of(1, 2), calls);
    }

    @Test
    public void replacingAndClearingEventsScriptDetachPreviousFilters() throws Exception {
        EventsApi events = new EventsApi();
        EventsScripting.INSTANCE.setScript(() -> events.modules.killAura.addTargetFilter(id -> false));
        ClientTickEndExecutor.instance.processQueue();
        assertFalse(getPredicate().test(1));

        EventsScripting.INSTANCE.setScript(() -> events.modules.killAura.addTargetFilter(id -> true));
        ClientTickEndExecutor.instance.processQueue();
        assertTrue(getPredicate().test(1));

        EventsScripting.INSTANCE.setScript(() -> {});
        ClientTickEndExecutor.instance.processQueue();
        assertNull(getPredicate());

        EventsScripting.INSTANCE.setScript(() -> events.modules.killAura.addTargetFilter(id -> false));
        ClientTickEndExecutor.instance.processQueue();
        EventsScripting.INSTANCE.setScript(null);
        ClientTickEndExecutor.instance.processQueue();
        assertNull(getPredicate());
    }

    private static KillAura.TargetPredicate getPredicate() throws Exception {
        Field field = KillAura.class.getDeclaredField("targetPredicate");
        field.setAccessible(true);
        return (KillAura.TargetPredicate) field.get(KillAura.INSTANCE);
    }

    @Test
    public void failedFilterDisablesTheWholeEventsInstallationUntilSaved() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        Runnable program = () -> {
            EventsScripting.INSTANCE.addOnTickEnd(calls::incrementAndGet);
            EventsScripting.INSTANCE.addAimAssistTargetPredicate(_ -> { calls.incrementAndGet(); return true; });
            EventsScripting.INSTANCE.addKillAuraTargetPredicate(_ -> { calls.incrementAndGet(); throw new IllegalStateException("filter failed"); });
        };
        EventsScripting.INSTANCE.setScript(program);
        ClientTickEndExecutor.instance.processQueue();
        ScriptActivation<?> failed = getActivation();
        KillAura.TargetPredicate old = getPredicate();
        assertFalse(old.test(1));
        assertFalse(failed.isActive());
        assertFalse(getAimPredicate().test(1));
        assertFalse(failed.run("tick", calls::incrementAndGet));
        assertEquals(1, calls.get());
        drainFailures();
        assertFalse(getPredicate().test(1));
        assertFalse(getAimPredicate().test(1));
        assertTrue(getCallbacks().isEmpty());

        EventsScripting.INSTANCE.setScript(program);
        ClientTickEndExecutor.instance.processQueue();
        assertTrue(getActivation().isActive());
        assertTrue(getAimPredicate().test(1));
        assertFalse(old.test(1));
        assertTrue(getActivation().isActive());
        assertEquals(2, calls.get());
    }

    @Test
    public void initializationFailureBlocksPartiallyRegisteredFiltersAndDetachesCallbacks() throws Exception {
        EventsScripting.INSTANCE.setScript(() -> {
            EventsScripting.INSTANCE.addOnTickEnd(() -> {});
            EventsScripting.INSTANCE.addKillAuraTargetPredicate(_ -> true);
            throw new IllegalStateException("initialization failed");
        });
        ClientTickEndExecutor.instance.processQueue();
        assertFalse(getActivation().isActive());
        drainFailures();
        assertFalse(getPredicate().test(1));
        assertTrue(getCallbacks().isEmpty());
    }

    @Test
    public void pendingFailureCleanupCannotDisableSavedReplacement() throws Exception {
        EventsScripting.INSTANCE.setScript(() -> EventsScripting.INSTANCE.addKillAuraTargetPredicate(_ -> { throw new IllegalStateException(); }));
        ClientTickEndExecutor.instance.processQueue();
        assertFalse(getPredicate().test(1));

        EventsScripting.INSTANCE.setScript(() -> EventsScripting.INSTANCE.addKillAuraTargetPredicate(_ -> true));
        ClientTickEndExecutor.instance.processQueue();
        drainFailures();
        assertTrue(getActivation().isActive());
        assertTrue(getPredicate().test(1));
    }

    private static ScriptActivation<?> getActivation() throws Exception {
        Field field = EventsScripting.class.getDeclaredField("script");
        field.setAccessible(true);
        return (ScriptActivation<?>) field.get(EventsScripting.INSTANCE);
    }

    private static AimAssist.TargetPredicate getAimPredicate() throws Exception {
        Field field = AimAssist.class.getDeclaredField("script");
        field.setAccessible(true);
        return (AimAssist.TargetPredicate) field.get(AimAssist.INSTANCE);
    }

    private static List<?> getCallbacks() throws Exception {
        Field field = EventsScripting.class.getDeclaredField("onTickEnd");
        field.setAccessible(true);
        return (List<?>) field.get(EventsScripting.INSTANCE);
    }

    private static void drainFailures() throws Exception {
        var method = EventsScripting.class.getDeclaredMethod("processPendingFailures");
        method.setAccessible(true);
        method.invoke(EventsScripting.INSTANCE);
    }
}