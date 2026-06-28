package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class EventsScriptingScriptSlot extends SingleScriptSlot {

    public EventsScriptingScriptSlot() {
        super(ScriptType.EVENTS);
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        ConfigStore.instance.getConfig().eventsScriptingConfig.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(ScriptType.EVENTS, code);
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        EventsScripting.instance.setScript((Runnable) program);
    }
}