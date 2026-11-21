package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.events.BlockPosConsumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BlockAutomationScriptSlot extends SingleScriptSlot {

    public BlockAutomationScriptSlot() {
        super(ScriptType.BLOCK_AUTOMATION);
    }

    @Override
    protected @Nullable String readConfigCode() {
        return BlockAutomation.INSTANCE.getSettings().get().code();
    }

    @Override
    protected void updateConfigCode(@Nullable String code) {
        BlockAutomation.INSTANCE.getSettings().update(config -> config.withCode(code));
    }

    @Override
    protected <T> void applyScript(@Nullable T program) {
        BlockAutomation.INSTANCE.setScript((BlockPosConsumer) program);
    }
}
