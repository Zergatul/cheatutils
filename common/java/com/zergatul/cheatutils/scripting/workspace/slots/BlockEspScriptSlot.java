package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import com.zergatul.cheatutils.modules.esp.block.BlockEsp;
import com.zergatul.cheatutils.modules.esp.block.BlockGroupKey;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.events.BlockEspConsumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public class BlockEspScriptSlot extends MultiScriptSlot {

    public BlockEspScriptSlot() {
        super(ScriptType.BLOCK_ESP);
    }

    @Override
    protected List<ConfigScript> readConfigScripts() {
        return BlockEsp.INSTANCE.getSettings().get().entries().stream()
                .map(e -> new ConfigScript(new BlockGroupKey(e.blocks()).toString(), e.code()))
                .toList();
    }

    @Override
    protected boolean hasConfigEntry(String identifier) {
        return BlockGroupKey.parse(identifier).findIn(BlockEsp.INSTANCE.getSettings().get().entries()) != null;
    }

    @Override
    protected void updateConfigCode(String identifier, @Nullable String code) {
        BlockGroupKey key = BlockGroupKey.parse(identifier);
        BlockEsp.INSTANCE.getSettings().update(config -> {
            BlockEspEntry entry = key.findIn(config.entries());
            ImmutableList<BlockEspEntry> entries = config.entries().replaceFirst(
                    e -> e == entry,
                    e -> e.withCode(code));
            return config.withEntries(entries);
        });
    }

    @Override
    protected <T> void applyScript(String identifier, @Nullable T program) {
        BlockEsp.INSTANCE.setScript(BlockGroupKey.parse(identifier), (BlockEspConsumer) program);
    }

    @Override
    protected void clearRuntime() {
        BlockEsp.INSTANCE.clearScripts();
    }

    @Override
    protected void moveRuntime(String oldIdentifier, String newIdentifier) {
        BlockEsp.INSTANCE.moveScript(BlockGroupKey.parse(oldIdentifier), BlockGroupKey.parse(newIdentifier));
    }
}
