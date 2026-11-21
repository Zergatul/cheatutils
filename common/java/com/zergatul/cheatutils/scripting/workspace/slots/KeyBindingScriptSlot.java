package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.modules.scripting.keybindings.KeyBindings;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public class KeyBindingScriptSlot extends MultiScriptSlot {

    public KeyBindingScriptSlot() {
        super(ScriptType.KEYBINDING);
    }

    @Override
    protected List<ConfigScript> readConfigScripts() {
        return KeyBindings.INSTANCE.getSettings().get().entries().stream()
                .map(e -> new ConfigScript(e.name(), e.code()))
                .toList();
    }

    @Override
    protected boolean hasConfigEntry(String identifier) {
        return KeyBindings.INSTANCE.getSettings().get().entries().contains(e -> e.name().equals(identifier));
    }

    @Override
    protected void updateConfigCode(String identifier, @Nullable String code) {
        KeyBindings.INSTANCE.getSettings().update(config -> config.withEntries(
                config.entries().replaceFirst(
                        e -> e.name().equals(identifier),
                        e -> e.withCode(code))));
    }

    @Override
    protected <T> void applyScript(String identifier, @Nullable T program) {
        KeyBindings.INSTANCE.setScript(identifier, (AsyncRunnable) program);
    }

    @Override
    protected void clearRuntime() {
        KeyBindings.INSTANCE.clearScripts();
    }

    @Override
    protected void moveRuntime(String oldIdentifier, String newIdentifier) {
        KeyBindings.INSTANCE.renameScript(oldIdentifier, newIdentifier);
    }
}
