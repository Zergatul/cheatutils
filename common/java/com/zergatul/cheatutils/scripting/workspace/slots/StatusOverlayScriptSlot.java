package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.ScriptType;
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
        ConfigStore.instance.getConfig().statusOverlayConfig.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptsController.instance.compileOverlay(code);
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        StatusOverlay.instance.setScript((Runnable) program);
    }
}