package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.Nullable;

public class EventsScriptingScriptSlot extends SingleScriptSlot {

    public EventsScriptingScriptSlot() {
        super(ScriptType.EVENTS);
    }

    @Override
    protected void onCodeChanged(@Nullable String code) {
        ConfigStore.instance.getConfig().eventsScriptingConfig.code = code;
    }

    @Override
    protected void onProgramChanged(@Nullable Object program) {
        EventsScripting.instance.setScript((Runnable) program);
    }
}