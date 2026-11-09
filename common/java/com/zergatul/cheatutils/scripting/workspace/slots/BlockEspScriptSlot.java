package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import com.zergatul.cheatutils.modules.esp.block.BlockEsp;
import com.zergatul.cheatutils.modules.esp.block.BlockGroupKey;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.events.BlockEspConsumer;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BlockEspScriptSlot extends MultiScriptSlot {

    public BlockEspScriptSlot() {
        super(ScriptType.BLOCK_ESP);
    }

    @Override
    public void clear() {
        BlockEsp.INSTANCE.clearScripts();
        clearDocuments();
    }

    @Override
    protected void updateConfigCode(String identifier, @Nullable String code) {
        BlockEsp.INSTANCE.getSettings().update(config -> {
            BlockGroupKey key = BlockGroupKey.parse(identifier);
            BlockEspEntry entry = key.findIn(config.entries());
            ImmutableList<BlockEspEntry> entries = config.entries().replaceFirst(
                    e -> e == entry,
                    e -> e.withCode(code));
            return config.withEntries(entries);
        });
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(ScriptType.BLOCK_ESP, code);
    }

    @Override
    protected <T> void applyScript(String identifier, @Nullable T program) {
        BlockGroupKey key = BlockGroupKey.parse(identifier);
        BlockEspEntry entry = key.findIn(BlockEsp.INSTANCE.getSettings().get().entries());
        if (entry == null) {
            throw new IllegalStateException();
        }

        BlockEsp.INSTANCE.setScript(entry, (BlockEspConsumer) program);
    }
}