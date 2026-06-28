package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;

public class BlockAutomationCodeApi extends CodeApiBase {

    @Override
    public String getRoute() {
        return "block-automation-code";
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.BLOCK_AUTOMATION;
    }
}