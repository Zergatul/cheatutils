package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
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
        assertNull(slot.getInstance(null).code);
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
        assertEquals("unknownName();", slot.getInstance(null).code);
    }

    private static class TestSlot extends SingleScriptSlot {

        private final List<@Nullable Object> applied = new ArrayList<>();

        private TestSlot() {
            super(ScriptType.OVERLAY);
        }

        private @Nullable Object current() {
            return applied.isEmpty() ? null : applied.getLast();
        }

        @Override
        protected void updateConfigCode(@Nullable String code) {}

        @Override
        protected CompilationResult compileScript(String code) {
            return new Compiler(ScriptType.OVERLAY.createParameters()).compile(code);
        }

        @Override
        protected <T> void applyScript(@Nullable T program) {
            applied.add(program);
        }
    }
}
