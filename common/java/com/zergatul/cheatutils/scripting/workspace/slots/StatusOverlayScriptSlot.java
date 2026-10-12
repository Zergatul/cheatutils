package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StatusOverlayScriptSlot extends SingleScriptSlot {

    public StatusOverlayScriptSlot() {
        super(ScriptType.OVERLAY);
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        StatusOverlay.INSTANCE.getSettings().update(c -> c.code = code);
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(ScriptType.OVERLAY, code);
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        StatusOverlay.INSTANCE.setScript((Runnable) program);
    }
}