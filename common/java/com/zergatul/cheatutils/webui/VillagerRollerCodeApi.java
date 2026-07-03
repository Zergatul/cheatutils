package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;

public class VillagerRollerCodeApi extends CodeApiBase {

    @Override
    public String getRoute() {
        return "villager-roller-code";
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.VILLAGER_ROLLER;
    }
}