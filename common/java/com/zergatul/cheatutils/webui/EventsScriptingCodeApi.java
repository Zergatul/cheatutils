package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;

public class EventsScriptingCodeApi extends CodeApiBase {

    @Override
    public String getRoute() {
        return "events-scripting-code";
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.EVENTS;
    }
}