package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.configs.KeyBindingScriptsConfig.ScriptEntry;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.input.Keyboard;

import java.util.*;

/** Script and assignment mutations run on the client thread. */
public class KeyBindings implements Module {
    public static final KeyBindings instance = new KeyBindings();
    private final KeyBinding[] keys = new KeyBinding[KeyBindingsConfig.KeysCount];
    private final Map<String, Runnable> scripts = new HashMap<>();
    private final Map<String, String> errors = new HashMap<>();

    private KeyBindings() {
        Events.RegisterKeyBindings.add(this::onRegisterKeyBindings);
        Events.AfterHandleKeyBindings.add(this::onHandleKeyBindings);
        Events.ConfigLoaded.add(this::onConfigLoaded);
    }

    public KeyBinding getKeyMappingByIndex(int index) { return keys[index]; }
    public String getError(String name) { return errors.get(name); }
    public List<ScriptEntry> list() { return new ArrayList<>(entries()); }
    public ScriptEntry get(String name) {
        return entries().stream().filter(entry -> entry.name.equals(name)).findFirst().orElse(null);
    }

    public List<DiagnosticMessage> add(String name, String code) {
        validate(name, code);
        if (get(name) != null) throw new IllegalArgumentException("Script with the same name already exists.");
        CompilationResult result = compile(code);
        if (result.getProgram() == null) return result.getDiagnostics();
        entries().add(new ScriptEntry(name, code));
        scripts.put(name, result.getProgram());
        errors.remove(name);
        ConfigStore.instance.requestWrite();
        return Collections.emptyList();
    }

    public List<DiagnosticMessage> update(String oldName, String name, String code) {
        validate(name, code);
        ScriptEntry entry = get(oldName);
        if (entry == null) throw new IllegalArgumentException("Script does not exist.");
        if (!oldName.equals(name) && get(name) != null) throw new IllegalArgumentException("Script with the same name already exists.");
        CompilationResult result = compile(code);
        if (result.getProgram() == null) return result.getDiagnostics();
        entry.name = name;
        entry.code = code;
        String[] bindings = bindings();
        for (int i = 0; i < bindings.length; i++) {
            if (oldName.equals(bindings[i])) bindings[i] = name;
        }
        scripts.remove(oldName);
        errors.remove(oldName);
        scripts.put(name, result.getProgram());
        ConfigStore.instance.requestWrite();
        return Collections.emptyList();
    }

    public void remove(String name) {
        if (get(name) == null) throw new IllegalArgumentException("Script does not exist.");
        assign(-1, name);
        entries().removeIf(entry -> entry.name.equals(name));
        scripts.remove(name);
        errors.remove(name);
        ConfigStore.instance.requestWrite();
    }

    public void assign(int index, String name) {
        if (index < -1 || index >= keys.length) throw new IllegalArgumentException("Invalid key index.");
        if (get(name) == null) throw new IllegalArgumentException("Script does not exist.");
        String[] bindings = bindings();
        for (int i = 0; i < bindings.length; i++) {
            if (name.equals(bindings[i])) bindings[i] = null;
        }
        if (index >= 0) bindings[index] = name;
        ConfigStore.instance.requestWrite();
    }

    public void execute(String name) {
        Runnable script = scripts.get(name);
        if (script == null) return;
        Throwable failure = ScriptExecutionManager.instance.execute(ScriptType.KEYBINDING, script);
        if (failure != null) {
            scripts.remove(name);
            errors.put(name, "Disabled after runtime failure. Save the script to re-enable it: " + failure);
            LogManager.getLogger(KeyBindings.class).error("Keybinding script '{}' disabled", name, failure);
        }
    }

    private void onConfigLoaded() {
        scripts.clear();
        errors.clear();
        for (ScriptEntry entry : entries()) {
            try {
                CompilationResult result = compile(entry.code);
                if (result.getProgram() != null) scripts.put(entry.name, result.getProgram());
                else errors.put(entry.name, "Compilation failed. Edit and save this script to re-enable it.");
            } catch (RuntimeException e) {
                errors.put(entry.name, "Compilation failed: " + e);
            }
            if (errors.containsKey(entry.name)) {
                LogManager.getLogger(KeyBindings.class).error("Keybinding script '{}': {}", entry.name, errors.get(entry.name));
            }
        }
        Set<String> assigned = new HashSet<>();
        String[] bindings = bindings();
        for (int i = 0; i < bindings.length; i++) {
            if (get(bindings[i]) == null || !assigned.add(bindings[i])) bindings[i] = null;
        }
        drainKeys();
    }

    private void onRegisterKeyBindings(IKeyBindingRegistry registry) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null) continue;
            keys[i] = new KeyBinding("key.zergatul.cheatutils.reserved" + i, KeyConflictContext.IN_GAME,
                    i == 1 ? Keyboard.KEY_F6 : Keyboard.KEY_NONE, "key.categories.cheatutils");
            registry.register(keys[i]);
        }
    }

    private void onHandleKeyBindings() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null || mc.currentScreen != null || !mc.inGameHasFocus) {
            drainKeys();
            return;
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null) continue;
            while (keys[i].isPressed()) execute(bindings()[i]);
        }
    }

    private void drainKeys() {
        for (KeyBinding key : keys) {
            if (key != null) while (key.isPressed()) { }
        }
    }

    private List<ScriptEntry> entries() { return ConfigStore.instance.getConfig().keyBindingScriptsConfig.scripts; }
    private String[] bindings() { return ConfigStore.instance.getConfig().keyBindingsConfig.bindings; }
    private CompilationResult compile(String code) { return ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code); }
    private void validate(String name, String code) {
        if (name == null || name.trim().isEmpty() || name.length() > 100) throw new IllegalArgumentException("Name must contain 1–100 characters.");
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("Code is required.");
    }
}
