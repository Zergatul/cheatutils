package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingScriptsConfig;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class KeyBindingScriptSlot extends MultiScriptSlot {

    public KeyBindingScriptSlot() {
        super(ScriptType.KEYBINDING);
    }

    @Override
    protected void updateConfigCode(String identifier, @Nullable String code) {
        KeyBindingScriptsConfig config = ConfigStore.instance.getConfig().keyBindingScriptsConfig;
        KeyBindingScriptsConfig.ScriptEntry entry = config.scripts.stream()
                .filter(e -> e.name.equals(identifier))
                .findFirst()
                .orElseThrow();
        entry.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code);
    }

    @Override
    protected <T> void applyScript(String identifier, @Nullable T program) {
        KeyBindings.instance.setScript(identifier, (AsyncRunnable) program);
    }
}