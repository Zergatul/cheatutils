package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class EventsScriptingScriptSlot extends SingleScriptSlot {

    public EventsScriptingScriptSlot() {
        super(ScriptType.EVENTS);
    }

    @Override
    protected @Nullable String readConfigCode() {
        return EventsScripting.INSTANCE.getSettings().get().code();
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        EventsScripting.INSTANCE.getSettings().update(config -> config.withCode(code));
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        EventsScripting.INSTANCE.setScript((Runnable) program);
    }
}
