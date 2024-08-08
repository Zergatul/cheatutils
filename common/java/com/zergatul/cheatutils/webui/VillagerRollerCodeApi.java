package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.scripting.compiler.CompilationResult;

public class VillagerRollerCodeApi extends CodeApiBase<Runnable> {

    @Override
    public String getRoute() {
        return "villager-roller-code";
    }

    @Override
    protected CompilationResult compile(String code) {
        return ScriptsController.instance.compileVillagerRoller(code);
    }

    @Override
    protected void setCode(String code) {
        ConfigStore.instance.getConfig().villagerRollerConfig.code = code;
    }

    @Override
    protected void setProgram(Runnable program) {
        VillagerRoller.instance.setScript(program);
    }
}