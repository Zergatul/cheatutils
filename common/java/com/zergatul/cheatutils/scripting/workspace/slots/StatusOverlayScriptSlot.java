package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.Nullable;

public class StatusOverlayScriptSlot extends SingleScriptSlot {
    public StatusOverlayScriptSlot() {
        super(ScriptType.OVERLAY);
    }

    @Override
    protected void onCodeChanged(@Nullable String code) {
        ConfigStore.instance.getConfig().statusOverlayConfig.code = code;
    }

    @Override
    protected void onProgramChanged(@Nullable Object program) {
        StatusOverlay.instance.setScript((Runnable) program);
    }
}