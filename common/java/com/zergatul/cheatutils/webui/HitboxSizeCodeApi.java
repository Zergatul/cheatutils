package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;

public class HitboxSizeCodeApi extends CodeApiBase {

    @Override
    public String getRoute() {
        return "hitbox-size-code";
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.HITBOX_SIZE;
    }
}