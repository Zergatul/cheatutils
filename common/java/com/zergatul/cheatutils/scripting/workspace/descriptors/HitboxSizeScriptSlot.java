package com.zergatul.cheatutils.scripting.workspace.descriptors;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.hacks.HitboxSize;
import com.zergatul.cheatutils.scripting.HitboxSizeFunction;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class HitboxSizeScriptSlot extends SingleScriptSlot {

    public HitboxSizeScriptSlot() {
        super(ScriptType.HITBOX_SIZE);
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        ConfigStore.instance.getConfig().hitboxSizeConfig.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptsController.instance.compileHitboxSize(code);
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        HitboxSize.instance.setScript((HitboxSizeFunction) program);
    }
}
