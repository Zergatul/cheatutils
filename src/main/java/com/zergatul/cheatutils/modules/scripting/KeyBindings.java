package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingScriptsConfig;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.scripting.*;
import com.zergatul.cheatutils.scripting.workspace.KeyBindingScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.utility.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.*;

@NullMarked
public class KeyBindings implements Module {

    public static final KeyBindings INSTANCE = new KeyBindings();
    public static final String CATEGORY = "key.categories.cheatutils";
    public static final String KEY_PREFIX = "key.cheatutils.reserved";

    private final Minecraft mc = Minecraft.getMinecraft();
    private final KeyBinding[] keys;
    private final Map<String, ScriptActivation<AsyncRunnable>> scripts;
    private final Optional<AsyncRunnable>[] actions;

    private KeyBindings() {
        this.scripts = new HashMap<>();

        this.keys = new KeyBinding[KeyBindingsConfig.KEYS_COUNT];
        for (int i = 0; i < keys.length; i++) {
            this.keys[i] = new KeyBinding(
                    KEY_PREFIX + i,
                    KeyConflictContext.IN_GAME,
                    Keyboard.KEY_NONE,
                    CATEGORY);
        }

        this.actions = createOptionalArray(KeyBindingsConfig.KEYS_COUNT);
        clear();

        Events.RegisterKeyBindings.add(this::onRegisterKeyBindings);
        Events.AfterHandleKeyBindings.add(this::onHandleKeyBindings);
    }

    public KeyBinding getKeyMappingByIndex(int index) {
        return this.keys[index];
    }

    public void clear() {
        scripts.values().forEach(ScriptActivation::deactivate);
        scripts.clear();
        Arrays.setAll(actions, index -> Optional.empty());
        slot().clear();
    }

    public List<Script> list() {
        return Lists.from(
                ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts
                        .stream()
                        .map(entry -> new Script(entry.name, entry.code, getAction(entry.name))));
    }

    public @Nullable Script get(String name) {
        return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts
                .stream()
                .filter(entry -> entry.name.equals(name))
                .findFirst()
                .map(entry -> new Script(entry.name, entry.code, getAction(entry.name)))
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

        return Lists.of();
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

            return Lists.of();
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
                .orElseThrow(NoSuchElementException::new);
        entry.name = newName;
        entry.code = code;

        @Nullable String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < bindings.length; i++) {
            if (oldName.equals(bindings[i])) {
                bindings[i] = newName;
            }
        }

        slot().remove(oldName);
        ScriptSaveResult result = slot().init(newName, code);
        if (!result.isSuccess()) {
            return result.getDiagnostics();
        }

        return Lists.of();
    }

    public void remove(String name) {
        assign(-1, name);
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

        if (0 <= index && index < KeyBindingsConfig.KEYS_COUNT) {
            AsyncRunnable compiled = getAction(name);
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
        ScriptActivation<AsyncRunnable> previous = scripts.remove(name);
        if (previous != null) {
            previous.deactivate();
        }
        if (script != null) {
            scripts.put(name, new ScriptActivation<>(new ScriptRef(ScriptType.KEYBINDING, name), script));
        }

        refreshAssignments(name);
    }

    private @Nullable AsyncRunnable getAction(String name) {
        ScriptActivation<AsyncRunnable> activation = scripts.get(name);
        return activation == null ? null : () -> activation.execute(activation.program);
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
        AsyncRunnable script = getAction(name);
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
            KeyBinding key = keys[i];
            Optional<AsyncRunnable> action = actions[i];
            while (key.isPressed()) {
                if (action.isPresent()) {
                    String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[i];
                    ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, Objects.requireNonNull(name));
                    ScriptExecutionManager.INSTANCE.execute(ref, action.get());
                }
            }
        }
    }

    private void onRegisterKeyBindings(IKeyBindingRegistry registry) {
        for (KeyBinding key : keys) {
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