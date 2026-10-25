package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
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
        VillagerRoller.INSTANCE.getSettings().update(config -> config.withCode(code));
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(ScriptType.VILLAGER_ROLLER, code);
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        VillagerRoller.INSTANCE.setScript((Runnable) program);
    }
}