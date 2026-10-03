package com.zergatul.cheatutils.modules.scripting;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.collections.OneOf3;
import com.zergatul.cheatutils.collections.SuccessfulResult;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingScriptsConfig;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.KeyBindingScriptSlot;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public class KeyBindings implements Module {

    public static final KeyBindings instance = new KeyBindings();

    private final Minecraft mc = Minecraft.getInstance();
    private final KeyMapping[] keys;
    private final Map<String, ScriptActivation<AsyncRunnable>> scripts;
    private final Optional<AsyncRunnable>[] actions;

    private KeyBindings() {
        Events.RegisterKeyBindings.add(this::onRegisterKeyBindings);
        Events.AfterHandleKeyBindings.add(this::onHandleKeyBindings);

        WebApiRegistry.INSTANCE.register(new KeyBindingScriptsWebApi());
        WebApiRegistry.INSTANCE.register(new ScriptsAssignWebApi());

        this.scripts = new HashMap<>();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "common"));
        this.keys = new KeyMapping[KeyBindingsConfig.KeysCount];
        for (int i = 0; i < keys.length; i++) {
            this.keys[i] = new KeyMapping("key.zergatul.cheatutils.reserved" + i, InputConstants.UNKNOWN.getValue(), category);
        }

        this.actions = createOptionalArray(KeyBindingsConfig.KeysCount);
        clear();
    }

    public KeyMapping getKeyMappingByIndex(int index) {
        return this.keys[index];
    }

    public void clear() {
        scripts.values().forEach(ScriptActivation::deactivate);
        scripts.clear();
        Arrays.setAll(actions, _ -> Optional.empty());
        slot().clear();
    }

    public List<Script> list() {
        return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts
                .stream()
                .map(entry -> new Script(entry.name, entry.code, getAction(entry.name)))
                .toList();
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

    public OneOf3<String, List<DiagnosticMessage>, SuccessfulResult> add(@Nullable String name, @Nullable String code, boolean addIfCompilationFails) throws IllegalArgumentException {
        if (name == null) {
            return OneOf3.from1("Name is required");
        }
        if (code == null || code.isEmpty()) {
            return OneOf3.from1("Code is required.");
        }
        if (exists(name)) {
            return OneOf3.from1("Script with the same name already exists.");
        }

        if (!addIfCompilationFails) {
            CompilationResult result = ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code);
            if (result.getProgram() == null) {
                return OneOf3.from2(Objects.requireNonNull(result.getDiagnostics()));
            }
        }

        ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.add(new KeyBindingScriptsConfig.ScriptEntry(name, code));
        ScriptSaveResult result = slot().init(name, code);
        if (!result.isSuccess()) {
            if (!addIfCompilationFails) {
                ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts.removeIf(entry -> entry.name.equals(name));
                slot().remove(name);
            }
            return OneOf3.from2(result.getDiagnostics());
        }

        return OneOf3.from3(SuccessfulResult.INSTANCE);
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

        slot().remove(oldName);
        ScriptSaveResult result = slot().init(newName, code);
        if (!result.isSuccess()) {
            return result.getDiagnostics();
        }

        return List.of();
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

        if (0 <= index && index < KeyBindingsConfig.KeysCount) {
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
            KeyMapping key = keys[i];
            Optional<AsyncRunnable> action = actions[i];
            while (key.consumeClick()) {
                if (action.isPresent()) {
                    @Nullable String name = ConfigStore.instance.getConfig().keyBindingsConfig.bindings[i];
                    ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, Objects.requireNonNull(name));
                    ScriptExecutionManager.instance.execute(ref, action.get());
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

    private static final class KeyBindingScriptsWebApi extends WebApiBase {

        @Override
        public String getRoute() {
            return "keybinding-scripts";
        }

        @Override
        public String get() {
            String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
            return gson.toJson(KeyBindings.instance.list().stream().map(s -> {
                int index = ArrayUtils.indexOf(bindings, s.name);
                return new Script(s.name, index);
            }).toArray());
        }

        @Override
        public String get(String id) {
            KeyBindings.Script script = KeyBindings.instance.get(id);
            if (script == null) {
                return gson.toJson((Object) null);
            } else {
                return gson.toJson(new Script(script));
            }
        }

        @Override
        public String put(String id, String body) {
            Script script = gson.fromJson(body, Script.class);
            List<DiagnosticMessage> messages = KeyBindings.instance.update(id, script.name, script.code);
            if (!messages.isEmpty()) {
                return gson.toJson(messages);
            }
            ConfigStore.instance.requestWrite();
            return "{ \"ok\": true }";
        }

        @Override
        public String post(String body) {
            Script script = gson.fromJson(body, Script.class);
            return KeyBindings.instance.add(script.name, script.code, false).match(
                    error -> gson.toJson(new ErrorResponse(error)),
                    diagnostics -> gson.toJson(diagnostics),
                    _ -> {
                        ConfigStore.instance.requestWrite();
                        return "{ \"ok\": true }";
                    });
        }

        @Override
        public String delete(String id) {
            KeyBindings.instance.remove(id);
            ConfigStore.instance.requestWrite();
            return "true";
        }

        public static class Script {

            public String name;
            public @Nullable String code;
            public int key;

            public Script(String name, int key) {
                this.name = name;
                this.key = key;
            }

            public Script(KeyBindings.Script script) {
                name = script.name;
                code = script.code;
            }
        }

        public record ErrorResponse(String error) {}
    }

    private static final class ScriptsAssignWebApi extends WebApiBase {

        @Override
        public String getRoute() {
            return "keybinding-scripts-assign";
        }

        @Override
        public String put(String id, String body) {
            int index = gson.fromJson(body, int.class);
            KeyBindings.instance.assign(index, id);
            ConfigStore.instance.requestWrite();
            return "true";
        }
    }
}