package com.zergatul.cheatutils.modules.scripting;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingScriptsConfig;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.KeyBindingScriptSlot;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

public class KeyBindings implements Module {

    public static final KeyBindings instance = new KeyBindings();

    private static final int RUNTIME_FAILURE_PRIORITY = Integer.MAX_VALUE;

    private final Minecraft mc = Minecraft.getInstance();
    private final KeyMapping[] keys;
    private final Map<String, AsyncRunnable> scripts = new HashMap<>();
    private final AsyncRunnable[] actions = new AsyncRunnable[KeyBindingsConfig.KeysCount];
    private final AtomicReference<RuntimeException> runtimeFailure = new AtomicReference<>();

    private KeyBindings() {
        keys = new KeyMapping[KeyBindingsConfig.KeysCount];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new KeyMapping("key.zergatul.cheatutils.reserved" + i, InputConstants.UNKNOWN.getValue(), "category.zergatul.cheatutils");
        }

        Events.RegisterKeyBindings.add(this::onRegisterKeyBindings);
        Events.AfterHandleKeyBindings.add(this::onHandleKeyBindings);
        Events.ClientTickEnd.add(this::onClientTickEnd, RUNTIME_FAILURE_PRIORITY);
    }

    public KeyMapping getKeyMappingByIndex(int index) {
        return keys[index];
    }

    public void clear() {
        scripts.clear();
        Arrays.fill(actions, null);
        slot().clearDocuments();
    }

    public List<Script> list() {
        return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.stream()
                .map(entry -> new Script(entry.name, entry.code, scripts.get(entry.name)))
                .toList();
    }

    public @Nullable Script get(String name) {
        return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.stream()
                .filter(entry -> Objects.equals(entry.name, name))
                .findFirst()
                .map(entry -> new Script(entry.name, entry.code, scripts.get(entry.name)))
                .orElse(null);
    }

    public boolean exists(String name) {
        return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.stream()
                .anyMatch(entry -> Objects.equals(entry.name, name));
    }

    public List<DiagnosticMessage> add(@Nullable String name, @Nullable String code, boolean addIfCompilationFails) {
        validateNewScript(name, code);

        if (!addIfCompilationFails) {
            CompilationResult result = ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code);
            if (result.getProgram() == null) {
                return Objects.requireNonNull(result.getDiagnostics());
            }
        }

        ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.add(
                new KeyBindingScriptsConfig.ScriptEntry(name, code));
        ScriptSaveResult result = slot().init(name, code);
        if (!result.isSuccess()) {
            if (!addIfCompilationFails) {
                ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.removeIf(
                        entry -> Objects.equals(entry.name, name));
                slot().remove(name);
            }
            return result.getDiagnostics();
        }

        return List.of();
    }

    public List<DiagnosticMessage> update(String oldName, String newName, @Nullable String code) {
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
            return result.isSuccess() ? List.of() : result.getDiagnostics();
        }

        CompilationResult compilation = ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code);
        if (compilation.getProgram() == null) {
            slot().save(oldName, code);
            return Objects.requireNonNull(compilation.getDiagnostics());
        }

        KeyBindingScriptsConfig.ScriptEntry entry = ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.stream()
                .filter(candidate -> Objects.equals(candidate.name, oldName))
                .findFirst()
                .orElseThrow();
        entry.name = newName;
        entry.code = code;

        String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < bindings.length; i++) {
            if (oldName.equals(bindings[i])) {
                bindings[i] = newName;
            }
        }

        scripts.remove(oldName);
        slot().remove(oldName);
        ScriptSaveResult result = slot().init(newName, code);
        return result.isSuccess() ? List.of() : result.getDiagnostics();
    }

    public void remove(String name) {
        assign(-1, name);
        scripts.remove(name);
        ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.removeIf(
                entry -> Objects.equals(entry.name, name));
        slot().remove(name);
    }

    public void assign(int index, String name) {
        String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < bindings.length; i++) {
            if (Objects.equals(bindings[i], name)) {
                actions[i] = null;
                bindings[i] = null;
            }
        }

        if (0 <= index && index < KeyBindingsConfig.KeysCount) {
            if (exists(name)) {
                actions[index] = scripts.get(name);
                bindings[index] = name;
            } else {
                actions[index] = null;
                bindings[index] = null;
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
        if (name == null || name.isBlank()) {
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
        String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < bindings.length; i++) {
            if (Objects.equals(bindings[i], name)) {
                actions[i] = script;
            }
        }
    }

    private void onHandleKeyBindings() {
        if (mc.player == null) {
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            KeyMapping key = keys[i];
            AsyncRunnable action = actions[i];
            while (key.consumeClick()) {
                if (action != null) {
                    ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, bindingsName(i));
                    if (!ScriptExecutionManager.instance.isRunning(ref)) {
                        try {
                            ScriptExecutionManager.instance.execute(ref, action)
                                    .whenComplete((result, throwable) -> crashOnRuntimeFailure(ref, throwable));
                        } catch (Throwable e) {
                            RuntimeException crash = createRuntimeFailure(ref, e);
                            if (crash != null) {
                                throw crash;
                            }
                        }
                    }
                }
            }
        }
    }

    private void onRegisterKeyBindings(IKeyBindingRegistry registry) {
        for (int i = 0; i < keys.length; i++) {
            registry.register(keys[i]);
        }
    }

    private String bindingsName(int index) {
        String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[index];
        return Objects.requireNonNull(name);
    }

    private void crashOnRuntimeFailure(ScriptRef ref, @Nullable Throwable throwable) {
        RuntimeException crash = createRuntimeFailure(ref, throwable);
        if (crash != null) {
            runtimeFailure.compareAndSet(null, crash);
        }
    }

    private void onClientTickEnd() {
        RuntimeException crash = runtimeFailure.getAndSet(null);
        if (crash != null) {
            throw crash;
        }
    }

    private @Nullable RuntimeException createRuntimeFailure(ScriptRef ref, @Nullable Throwable throwable) {
        Throwable failure = throwable;
        while (failure instanceof CompletionException && failure.getCause() != null) {
            failure = failure.getCause();
        }
        if (failure == null || failure instanceof CancellationException) {
            return null;
        }
        return new RuntimeException("Key binding script '" + ref.identifier() + "' failed.", failure);
    }

    private KeyBindingScriptSlot slot() {
        return (KeyBindingScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.KEYBINDING);
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