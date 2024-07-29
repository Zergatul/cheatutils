package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptController;
import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.scripting.compiler.CompilationResult;

public class BlockAutomationCodeApi extends CodeApiBase {

    @Override
    public String getRoute() {
        return "block-automation-code";
    }

    @Override
    protected CompilationResult<Runnable> compile(String code) {
        return ScriptController.instance.compileBlockAutomation(code);
    }

    @Override
    protected void setCode(String code) {
        ConfigStore.instance.getConfig().blockAutomationConfig.code = code;
    }

    @Override
    protected void setProgram(Runnable program) {
        BlockAutomation.instance.setScript(program);
    }
}