package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.slots.MultiScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.slots.SingleScriptSlot;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.InstanceCompilationResult;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptWorkspaceTests {

    private static final String CODE = "int x = 1;";

    @Test
    public void saveIsAppliedToDocumentSettingsAndRuntimeTogether() {
        TestSingleSlot slot = new TestSingleSlot();
        ScriptWorkspace workspace = new ScriptWorkspace(Map.of(ScriptType.OVERLAY, slot), () -> 0);

        assertTrue(workspace.save(new ScriptRef(ScriptType.OVERLAY), CODE).isSuccess());

        assertEquals(CODE, slot.configCode);
        assertEquals(CODE, slot.getInstance(null).code());
        assertNotNull(slot.current());
    }

    @Test
    public void saveRacingProfileSwitchIsIgnored() {
        AtomicLong generation = new AtomicLong();
        TestSingleSlot slot = new TestSingleSlot();
        // the profile is switched while the save is compiling
        slot.onCompile = generation::incrementAndGet;
        ScriptWorkspace workspace = new ScriptWorkspace(Map.of(ScriptType.OVERLAY, slot), generation::get);

        assertThrows(ScriptSaveRejectedException.class, () -> workspace.save(new ScriptRef(ScriptType.OVERLAY), CODE));

        assertNull(slot.configCode);
        assertNull(slot.getInstance(null).code());
        assertNull(slot.getInstance(null).lastAttemptCode());
        assertTrue(slot.applied.isEmpty());
    }

    @Test
    public void saveForDeletedEntryIsIgnored() {
        TestMultiSlot slot = new TestMultiSlot();
        slot.configCode.put("a", null);
        ScriptWorkspace workspace = new ScriptWorkspace(Map.of(ScriptType.ENTITY_ESP, slot), () -> 0);
        // the entry is deleted while the save is compiling
        slot.onCompile = () -> slot.configCode.remove("a");

        assertThrows(ScriptSaveRejectedException.class, () -> workspace.save(new ScriptRef(ScriptType.ENTITY_ESP, "a"), CODE));

        assertTrue(slot.configCode.isEmpty());
        assertTrue(slot.getInstances().isEmpty());
        assertTrue(slot.runtime.isEmpty());
    }

    @Test
    public void reloadReplacesEveryEntryWithCurrentSettings() {
        TestMultiSlot slot = new TestMultiSlot();
        slot.configCode.put("a", CODE);
        slot.configCode.put("b", CODE);
        slot.reload();
        assertEquals(List.of("a", "b"), List.copyOf(slot.runtime.keySet()));

        slot.configCode.clear();
        slot.configCode.put("c", CODE);
        slot.reload();

        assertEquals(List.of("c"), List.copyOf(slot.runtime.keySet()));
        assertEquals(List.of("c"), slot.getInstances().stream().map(d -> d.ref().identifier()).toList());
    }

    @Test
    public void brokenSlotDoesNotStopReloadOfOtherSlots() {
        TestMultiSlot broken = new TestMultiSlot() {
            @Override
            protected void clearRuntime() {
                throw new IllegalStateException("broken");
            }
        };
        TestSingleSlot slot = new TestSingleSlot();
        slot.configCode = CODE;
        Map<ScriptType, ScriptSlot> slots = new LinkedHashMap<>();
        slots.put(ScriptType.ENTITY_ESP, broken);
        slots.put(ScriptType.OVERLAY, slot);
        ScriptWorkspace workspace = new ScriptWorkspace(slots, () -> 0);

        workspace.reloadAll();

        assertNotNull(slot.current());
    }

    private static InstanceCompilationResult compileOverlay(String code) {
        return new Compiler(ScriptType.OVERLAY.createParameters()).compileInstance(code);
    }

    private static class TestSingleSlot extends SingleScriptSlot {

        private final List<@Nullable Object> applied = new ArrayList<>();
        private @Nullable String configCode;
        private Runnable onCompile = () -> {};

        private TestSingleSlot() {
            super(ScriptType.OVERLAY);
        }

        private @Nullable Object current() {
            return applied.isEmpty() ? null : applied.getLast();
        }

        @Override
        public InstanceCompilationResult compileScript(String code) {
            onCompile.run();
            return compileOverlay(code);
        }

        @Override
        protected @Nullable String readConfigCode() {
            return configCode;
        }

        @Override
        protected void updateConfigCode(@Nullable String code) {
            configCode = code;
        }

        @Override
        protected <T> void applyScript(@Nullable T program) {
            applied.add(program);
        }
    }

    private static class TestMultiSlot extends MultiScriptSlot {

        private final Map<String, @Nullable String> configCode = new LinkedHashMap<>();
        private final Map<String, Object> runtime = new LinkedHashMap<>();
        private Runnable onCompile = () -> {};

        private TestMultiSlot() {
            super(ScriptType.ENTITY_ESP);
        }

        @Override
        public InstanceCompilationResult compileScript(String code) {
            onCompile.run();
            return compileOverlay(code);
        }

        @Override
        protected List<ConfigScript> readConfigScripts() {
            return configCode.entrySet().stream().map(e -> new ConfigScript(e.getKey(), e.getValue())).toList();
        }

        @Override
        protected boolean hasConfigEntry(String identifier) {
            return configCode.containsKey(identifier);
        }

        @Override
        protected void updateConfigCode(String identifier, @Nullable String code) {
            configCode.replace(identifier, code);
        }

        @Override
        protected <T> void applyScript(String identifier, @Nullable T program) {
            if (program == null) {
                runtime.remove(identifier);
            } else {
                runtime.put(identifier, program);
            }
        }

        @Override
        protected void clearRuntime() {
            runtime.clear();
        }
    }
}
