package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.modules.KeyBindingEntry;
import com.zergatul.cheatutils.modules.scripting.keybindings.KeyBindings;
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
        KeyBindings.INSTANCE.getSettings().update(config -> {
            KeyBindingEntry entry = config.entries().stream()
                    .filter(e -> e.name().equals(identifier))
                    .findFirst()
                    .orElseThrow();
            return config.withEntries(
                    config.entries().replaceFirst(
                            e -> e == entry,
                            e -> e.withCode(code)));
        });
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code);
    }

    @Override
    protected <T> void applyScript(String identifier, @Nullable T program) {
        KeyBindings.INSTANCE.setScript(identifier, (AsyncRunnable) program);
    }
}