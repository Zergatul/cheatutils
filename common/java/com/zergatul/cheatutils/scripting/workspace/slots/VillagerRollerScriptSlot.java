package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.Nullable;

public class VillagerRollerScriptSlot extends SingleScriptSlot {
    public VillagerRollerScriptSlot() {
        super(ScriptType.VILLAGER_ROLLER);
    }

    @Override
    protected void onCodeChanged(@Nullable String code) {
        ConfigStore.instance.getConfig().villagerRollerConfig.code = code;
    }

    @Override
    protected void onProgramChanged(@Nullable Object program) {
        VillagerRoller.instance.setScript((Runnable) program);
    }
}