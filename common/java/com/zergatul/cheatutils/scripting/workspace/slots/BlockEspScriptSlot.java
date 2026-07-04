package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.BlocksConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.esp.BlockEsp;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.events.BlockEspConsumer;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BlockEspScriptSlot extends MultiScriptSlot {

    public BlockEspScriptSlot() {
        super(ScriptType.BLOCK_ESP);
    }

    @Override
    public void clear() {
        BlockEsp.instance.clearScripts();
        clearDocuments();
    }

    @Override
    protected void updateConfigCode(String identifier, @Nullable String code) {
        BlockEspConfig config = findConfig(identifier);
        config.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptsController.instance.compileBlockEsp(code);
    }

    @Override
    protected <T> void applyScript(String identifier, @Nullable T program) {
        BlockEspConfig config = findConfig(identifier);
        BlockEsp.instance.setScript(config, (BlockEspConsumer) program);
    }

    private BlockEspConfig findConfig(String identifier) {
        Identifier id = Identifier.tryParse(identifier);
        if (id == null) {
            throw new IllegalStateException("Cannot parse identifier: " + identifier);
        }

        Block block = Registries.BLOCKS.getValue(id);
        if (block == null || block == Blocks.AIR) {
            throw new IllegalStateException("Invalid block id: " + identifier);
        }

        BlocksConfig config = ConfigStore.instance.getConfig().blocks;
        return config.getBlockConfigs().stream()
                .filter(c -> c.blocks.contains(block))
                .findFirst()
                .orElseThrow();
    }
}