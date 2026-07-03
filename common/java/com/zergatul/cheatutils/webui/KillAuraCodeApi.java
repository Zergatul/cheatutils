package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;

public class KillAuraCodeApi extends CodeApiBase {

    @Override
    public String getRoute() {
        return "kill-aura-code";
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.KILL_AURA;
    }
}