package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.modules.hacks.KillAura;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
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

import static org.junit.jupiter.api.Assertions.*;

public class KillAuraEventsTests {

    @BeforeAll
    public static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    public void clear() {
        EventsScripting.instance.clear();
        ClientTickEndExecutor.instance.processQueue();
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
        EventsScripting.instance.setScript(() -> {
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
        EventsScripting.instance.setScript(() -> events.modules.killAura.addTargetFilter(id -> false));
        ClientTickEndExecutor.instance.processQueue();
        assertFalse(getPredicate().test(1));

        EventsScripting.instance.setScript(() -> events.modules.killAura.addTargetFilter(id -> true));
        ClientTickEndExecutor.instance.processQueue();
        assertTrue(getPredicate().test(1));

        EventsScripting.instance.setScript(() -> {});
        ClientTickEndExecutor.instance.processQueue();
        assertNull(getPredicate());

        EventsScripting.instance.setScript(() -> events.modules.killAura.addTargetFilter(id -> false));
        ClientTickEndExecutor.instance.processQueue();
        EventsScripting.instance.setScript(null);
        ClientTickEndExecutor.instance.processQueue();
        assertNull(getPredicate());
    }

    private static KillAura.TargetPredicate getPredicate() throws Exception {
        Field field = KillAura.class.getDeclaredField("targetPredicate");
        field.setAccessible(true);
        return (KillAura.TargetPredicate) field.get(KillAura.instance);
    }
}
