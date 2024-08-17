package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.hacks.KillAura;
import com.zergatul.cheatutils.scripting.KillAuraFunction;
import com.zergatul.scripting.compiler.CompilationResult;

public class KillAuraCodeApi extends CodeApiBase<KillAuraFunction> {

    @Override
    public String getRoute() {
        return "kill-aura-code";
    }

    @Override
    protected CompilationResult compile(String code) {
        return ScriptsController.instance.compileKillAura(code);
    }

    @Override
    protected void setCode(String code) {
        ConfigStore.instance.getConfig().killAuraConfig.code = code;
    }

    @Override
    protected void setProgram(KillAuraFunction program) {
        KillAura.instance.setScript(program);
    }
}