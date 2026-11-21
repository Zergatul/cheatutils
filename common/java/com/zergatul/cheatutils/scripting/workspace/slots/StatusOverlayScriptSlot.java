package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StatusOverlayScriptSlot extends SingleScriptSlot {

    public StatusOverlayScriptSlot() {
        super(ScriptType.OVERLAY);
    }

    @Override
    protected @Nullable String readConfigCode() {
        return StatusOverlay.INSTANCE.getSettings().get().code();
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        StatusOverlay.INSTANCE.getSettings().update(config -> config.withCode(code));
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        StatusOverlay.INSTANCE.setScript((Runnable) program);
    }
}
