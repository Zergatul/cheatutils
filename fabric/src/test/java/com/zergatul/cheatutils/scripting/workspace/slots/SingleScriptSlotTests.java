package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.InstanceCompilationResult;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SingleScriptSlotTests {

    @Test
    public void startupInitializationAlwaysRetiresThePreviousProgram() {
        TestSlot slot = new TestSlot();
        assertTrue(slot.init("int x = 1;").isSuccess());
        assertNotNull(slot.current());

        assertTrue(slot.init(null).isSuccess());
        assertNull(slot.current());
        assertNull(slot.getInstance(null).code());
    }

    @Test
    public void startupCompilationFailureDoesNotLeavePreviousProgramInstalled() {
        TestSlot slot = new TestSlot();
        assertTrue(slot.init("int x = 1;").isSuccess());
        slot.applied.clear();

        var result = slot.init("unknownName();");
        assertFalse(result.isSuccess());
        assertEquals(1, slot.applied.size());
        assertNull(slot.current());
        assertEquals("unknownName();", slot.getInstance(null).code());
    }

    @Test
    public void reloadInstallsCodeFromCurrentSettings() {
        TestSlot slot = new TestSlot();
        slot.configCode = "int x = 1;";
        slot.reload();
        assertNotNull(slot.current());
        assertEquals("int x = 1;", slot.getInstance(null).code());

        slot.configCode = null;
        slot.reload();
        assertNull(slot.current());
        assertNull(slot.getInstance(null).code());
    }

    @Test
    public void invalidSaveKeepsLastWorkingProgramAndSettings() {
        TestSlot slot = new TestSlot();
        assertTrue(slot.commit(null, "int x = 1;", slot.compileScript("int x = 1;")).isSuccess());
        Object working = slot.current();
        assertNotNull(working);
        assertEquals("int x = 1;", slot.configCode);

        var result = slot.commit(null, "unknownName();", slot.compileScript("unknownName();"));
        assertFalse(result.isSuccess());
        assertSame(working, slot.current());
        assertEquals("int x = 1;", slot.configCode);
        assertEquals("int x = 1;", slot.getInstance(null).code());
        assertEquals("unknownName();", slot.getInstance(null).lastAttemptCode());
        assertNotNull(slot.getInstance(null).lastAttemptAt());
    }

    @Test
    public void emptySaveRemovesProgramAndSettingsCode() {
        TestSlot slot = new TestSlot();
        assertTrue(slot.commit(null, "int x = 1;", slot.compileScript("int x = 1;")).isSuccess());

        assertTrue(slot.commit(null, "", null).isSuccess());
        assertNull(slot.current());
        assertNull(slot.configCode);
        assertNull(slot.getInstance(null).code());
    }

    private static class TestSlot extends SingleScriptSlot {

        private final List<@Nullable Object> applied = new ArrayList<>();
        private @Nullable String configCode;

        private TestSlot() {
            super(ScriptType.OVERLAY);
        }

        private @Nullable Object current() {
            return applied.isEmpty() ? null : applied.getLast();
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
        public InstanceCompilationResult compileScript(String code) {
            return new Compiler(ScriptType.OVERLAY.createParameters()).compileInstance(code);
        }

        @Override
        protected <T> void applyScript(@Nullable T program) {
            applied.add(program);
        }
    }
}
