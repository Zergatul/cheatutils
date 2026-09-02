package com.zergatul.cheatutils.modules.scripting;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingScriptsConfig;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.KeyBindingScriptSlot;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public class KeyBindings implements Module {

    public static final KeyBindings instance = new KeyBindings();

    private final Minecraft mc = Minecraft.getInstance();
    private final KeyMapping[] keys;
    private final Map<String, AsyncRunnable> scripts;
    private final Optional<AsyncRunnable>[] actions;

    private KeyBindings() {
        this.scripts = new HashMap<>();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "common"));
        this.keys = new KeyMapping[KeyBindingsConfig.KeysCount];
        for (int i = 0; i < keys.length; i++) {
            this.keys[i] = new KeyMapping("key.zergatul.cheatutils.reserved" + i, InputConstants.UNKNOWN.getValue(), category);
        }

        this.actions = createOptionalArray(KeyBindingsConfig.KeysCount);
        clear();

        Events.RegisterKeyBindings.add(this::onRegisterKeyBindings);
        Events.AfterHandleKeyBindings.add(this::onHandleKeyBindings);
    }

    public KeyMapping getKeyMappingByIndex(int index) {
        return this.keys[index];
    }

    public void clear() {
        scripts.clear();
        Arrays.setAll(actions, _ -> Optional.empty());
        slot().clear();
    }

    public List<Script> list() {
        return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts
                .stream()
                .map(entry -> new Script(entry.name, entry.code, scripts.get(entry.name)))
                .toList();
    }

    public @Nullable Script get(String name) {
        return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts
                .stream()
                .filter(entry -> entry.name.equals(name))
                .findFirst()
                .map(entry -> new Script(entry.name, entry.code, scripts.get(entry.name)))
                .orElse(null);
    }

    public boolean exists(String name) {
        return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts
                .stream()
                .anyMatch(entry -> entry.name.equals(name));
    }

    public List<DiagnosticMessage> add(@Nullable String name, @Nullable String code, boolean addIfCompilationFails) throws IllegalArgumentException {
        validateNewScript(name, code);

        if (!addIfCompilationFails) {
            CompilationResult result = ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code);
            if (result.getProgram() == null) {
                assert result.getDiagnostics() != null;
                return result.getDiagnostics();
            }
        }

        ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.add(new KeyBindingScriptsConfig.ScriptEntry(name, code));
        ScriptSaveResult result = slot().init(name, code);
        if (!result.isSuccess()) {
            if (!addIfCompilationFails) {
                ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.removeIf(entry -> entry.name.equals(name));
                slot().remove(name);
            }
            return result.getDiagnostics();
        }

        return List.of();
    }

    public List<DiagnosticMessage> update(String oldName, String newName, @Nullable String code) throws IllegalArgumentException {
        if (!oldName.equals(newName) && exists(newName)) {
            throw new IllegalArgumentException("Script with the same name already exists.");
        }
        if (!exists(oldName)) {
            throw new IllegalArgumentException("Cannot find original script by name " + oldName + ".");
        }
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Code is required.");
        }

        if (oldName.equals(newName)) {
            ScriptSaveResult result = slot().save(oldName, code);
            if (!result.isSuccess()) {
                return result.getDiagnostics();
            }

            return List.of();
        }

        CompilationResult compilationResult = ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code);
        if (compilationResult.getProgram() == null) {
            slot().save(oldName, code);

            assert compilationResult.getDiagnostics() != null;
            return compilationResult.getDiagnostics();
        }

        KeyBindingScriptsConfig.ScriptEntry entry = ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts
                .stream()
                .filter(e -> e.name.equals(oldName))
                .findFirst()
                .orElseThrow();
        entry.name = newName;
        entry.code = code;

        @Nullable String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < bindings.length; i++) {
            if (oldName.equals(bindings[i])) {
                bindings[i] = newName;
            }
        }

        scripts.remove(oldName);
        slot().remove(oldName);
        ScriptSaveResult result = slot().init(newName, code);
        if (!result.isSuccess()) {
            return result.getDiagnostics();
        }

        return List.of();
    }

    public void remove(String name) {
        assign(-1, name);
        scripts.remove(name);
        slot().remove(name);
        ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.removeIf(entry -> entry.name.equals(name));
    }

    public void assign(int index, String name) {
        @Nullable String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < bindings.length; i++) {
            String binding = bindings[i];
            if (binding != null && binding.equals(name)) {
                actions[i] = Optional.empty();
                bindings[i] = null;
            }
        }

        if (0 <= index && index < KeyBindingsConfig.KeysCount) {
            AsyncRunnable compiled = scripts.get(name);
            if (compiled == null) {
                actions[index] = Optional.empty();
                bindings[index] = null;
            } else {
                actions[index] = Optional.of(compiled);
                bindings[index] = name;
            }
        }
    }

    public void setScript(String name, @Nullable AsyncRunnable script) {
        if (script == null) {
            scripts.remove(name);
        } else {
            scripts.put(name, script);
        }

        refreshAssignments(name);
    }

    private void validateNewScript(@Nullable String name, @Nullable String code) {
        if (name == null) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Code is required.");
        }
        if (exists(name)) {
            throw new IllegalArgumentException("Script with the same name already exists.");
        }
    }

    private void refreshAssignments(String name) {
        AsyncRunnable script = scripts.get(name);
        @Nullable String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < bindings.length; i++) {
            String binding = bindings[i];
            if (binding != null && binding.equals(name)) {
                actions[i] = script == null ? Optional.empty() : Optional.of(script);
            }
        }
    }

    private void onHandleKeyBindings() {
        if (mc.player == null) {
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            KeyMapping key = keys[i];
            Optional<AsyncRunnable> action = actions[i];
            while (key.consumeClick()) {
                if (action.isPresent()) {
                    @Nullable String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[i];
                    ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, Objects.requireNonNull(name));
                    try {
                        ScriptExecutionManager.instance.execute(
                                ref,
                                action.get(),
                                throwable -> ScriptRuntimeFailureHandler.instance.report(
                                        "Key binding script '" + ref.identifier() + "' failed.",
                                        throwable));
                    } catch (Throwable e) {
                        ScriptRuntimeFailureHandler.instance.throwIfFailure(
                                "Key binding script '" + ref.identifier() + "' failed.",
                                e);
                    }
                }
            }
        }
    }

    private void onRegisterKeyBindings(IKeyBindingRegistry registry) {
        for (KeyMapping key : keys) {
            registry.register(key);
        }
    }

    private KeyBindingScriptSlot slot() {
        return (KeyBindingScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.KEYBINDING);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T>[] createOptionalArray(int length) {
        return new Optional[length];
    }

    public static class Script {
        public String name;
        public String code;
        public @Nullable AsyncRunnable compiled;

        public Script(String name, String code, @Nullable AsyncRunnable compiled) {
            this.name = name;
            this.code = code;
            this.compiled = compiled;
        }
    }
}