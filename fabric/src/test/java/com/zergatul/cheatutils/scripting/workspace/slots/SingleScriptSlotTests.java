package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.scripting.ScriptInitializationException;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveRejectedException;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.ClassCompilationResult;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SingleScriptSlotTests {

    private static final String FAILING_INITIALIZER = "static int zero = 0; static int x = 1 / zero;";

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
    public void compilationDoesNotRunStaticInitializers() {
        TestSlot slot = new TestSlot();
        assertTrue(slot.compileScript(FAILING_INITIALIZER).isSuccess());
    }

    @Test
    public void saveWithFailingStaticInitializerIsRejectedAndKeepsWorkingProgram() {
        TestSlot slot = new TestSlot();
        assertTrue(slot.commit(null, "int x = 1;", slot.compileScript("int x = 1;")).isSuccess());
        Object working = slot.current();
        slot.applied.clear();

        assertThrows(ScriptSaveRejectedException.class,
                () -> slot.commit(null, FAILING_INITIALIZER, slot.compileScript(FAILING_INITIALIZER)));

        assertTrue(slot.applied.isEmpty());
        assertNotNull(working);
        assertEquals("int x = 1;", slot.configCode);
        assertEquals("int x = 1;", slot.getInstance(null).code());
        assertEquals("int x = 1;", slot.getInstance(null).lastAttemptCode());
    }

    @Test
    public void startupWithFailingStaticInitializerRetiresPreviousProgram() {
        TestSlot slot = new TestSlot();
        assertTrue(slot.init("int x = 1;").isSuccess());

        assertThrows(ScriptInitializationException.class, () -> slot.init(FAILING_INITIALIZER));

        assertNull(slot.current());
        assertEquals(FAILING_INITIALIZER, slot.getInstance(null).code());
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
        public ClassCompilationResult compileScript(String code) {
            return new Compiler(ScriptType.OVERLAY.createParameters()).compileClass(code);
        }

        @Override
        protected <T> void applyScript(@Nullable T program) {
            applied.add(program);
        }
    }
}
