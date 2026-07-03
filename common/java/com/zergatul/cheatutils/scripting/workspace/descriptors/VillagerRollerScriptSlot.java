package com.zergatul.cheatutils.scripting.workspace.descriptors;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class VillagerRollerScriptSlot extends SingleScriptSlot {

    public VillagerRollerScriptSlot() {
        super(ScriptType.VILLAGER_ROLLER);
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        ConfigStore.instance.getConfig().villagerRollerConfig.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptsController.instance.compileVillagerRoller(code);
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        VillagerRoller.instance.setScript((Runnable) program);
    }
}
