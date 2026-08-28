package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.events.BlockPosConsumer;
import org.jspecify.annotations.Nullable;

public class BlockAutomationScriptSlot extends SingleScriptSlot {
    public BlockAutomationScriptSlot() {
        super(ScriptType.BLOCK_AUTOMATION);
    }

    @Override
    protected void onCodeChanged(@Nullable String code) {
        ConfigStore.instance.getConfig().blockAutomationConfig.code = code;
    }

    @Override
    protected void onProgramChanged(@Nullable Object program) {
        BlockAutomation.instance.setScript((BlockPosConsumer) program);
    }
}