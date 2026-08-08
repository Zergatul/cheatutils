package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.Nullable;

public class KeyBindingScriptSlot extends MultiScriptSlot {
    public KeyBindingScriptSlot() {
        super(ScriptType.KEYBINDING);
    }

    @Override
    protected void onCodeChanged(String identifier, @Nullable String code) {
        ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.stream()
                .filter(entry -> identifier.equals(entry.name))
                .findFirst()
                .ifPresent(entry -> entry.code = code);
    }

    @Override
    protected void onProgramChanged(String identifier, @Nullable Object program) {
        KeyBindings.instance.setScript(identifier, (AsyncRunnable) program);
    }
}