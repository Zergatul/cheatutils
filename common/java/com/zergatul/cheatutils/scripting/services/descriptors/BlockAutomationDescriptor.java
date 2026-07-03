package com.zergatul.cheatutils.scripting.services.descriptors;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.events.BlockPosConsumer;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BlockAutomationDescriptor extends SingleScriptStorageDescriptor {

    public BlockAutomationDescriptor() {
        super(ScriptType.BLOCK_AUTOMATION);
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        ConfigStore.instance.getConfig().blockAutomationConfig.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptsController.instance.compileBlockAutomation(code);
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        BlockAutomation.instance.setScript((BlockPosConsumer) program);
    }
}