package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.scripting.compiler.CompilationResult;

public class EventsScriptingCodeApi extends CodeApiBase<Runnable> {

    @Override
    public String getRoute() {
        return "events-scripting-code";
    }

    @Override
    protected CompilationResult compile(String code) {
        return ScriptsController.instance.compileEvents(code);
    }

    @Override
    protected void setCode(String code) {
        ConfigStore.instance.getConfig().eventsScriptingConfig.code = code;
    }

    @Override
    protected void setProgram(Runnable program) {
        EventsScripting.instance.setScript(program);
    }
}