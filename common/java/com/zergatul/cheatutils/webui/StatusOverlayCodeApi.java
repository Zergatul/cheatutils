package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;

public class StatusOverlayCodeApi extends CodeApiBase {

    @Override
    public String getRoute() {
        return "status-overlay-code";
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.OVERLAY;
    }
}