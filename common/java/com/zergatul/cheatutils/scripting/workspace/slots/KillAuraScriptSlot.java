package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.hacks.KillAura;
import com.zergatul.cheatutils.scripting.KillAuraFunction;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class KillAuraScriptSlot extends SingleScriptSlot {

    public KillAuraScriptSlot() {
        super(ScriptType.KILL_AURA);
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        ConfigStore.instance.getConfig().killAuraConfig.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptsController.instance.compileKillAura(code);
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        KillAura.instance.setScript((KillAuraFunction) program);
    }
}
