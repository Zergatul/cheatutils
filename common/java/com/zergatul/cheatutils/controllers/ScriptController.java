package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.configs.ScriptsConfig;
import com.zergatul.cheatutils.scripting.CompilerFactory;
import com.zergatul.cheatutils.scripting.VisibilityCheck;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptController {

    public static final ScriptController instance = new ScriptController();

    private final Compiler handleKeybindingsCompiler =
            CompilerFactory.create(VisibilityCheck.getTypes("keybindings"));
    private final Compiler overlayCompiler =
            CompilerFactory.create(VisibilityCheck.getTypes("overlay"));
    private final Compiler blockAutomationCompiler =
            CompilerFactory.create(VisibilityCheck.getTypes("block-automation"));
    private final Compiler villagerRollerCompiler =
            CompilerFactory.create(VisibilityCheck.getTypes("villager-roller"));
    private final Compiler eventsCompiler =
            CompilerFactory.create(VisibilityCheck.getTypes("events"));
    private final Compiler entityEspCompiler =
            CompilerFactory.create(VisibilityCheck.getTypes("entity-esp"));

    private final List<Script> scripts = Collections.synchronizedList(new ArrayList<>());

    private ScriptController() {

    }

    public List<DiagnosticMessage> add(String name, String code, boolean addIfCompilationFails) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Code is required.");
        }
        if (exists(name)) {
            throw new IllegalArgumentException("Script with the same name already exists.");
        }

        var script = new Script();
        script.name = name;
        script.code = code;

        if (addIfCompilationFails) {
            scripts.add(script);
            ConfigStore.instance.getConfig().scriptsConfig.scripts.add(new ScriptsConfig.ScriptEntry(name, code));
        }

        CompilationResult<Runnable> result = handleKeybindingsCompiler.compile(code, Runnable.class);
        if (result.program() != null) {
            script.compiled = result.program();

            if (!addIfCompilationFails) {
                scripts.add(script);
                ConfigStore.instance.getConfig().scriptsConfig.scripts.add(new ScriptsConfig.ScriptEntry(name, code));
            }
        }

        if (result.diagnostics() != null) {
            return result.diagnostics();
        } else {
            return List.of();
        }
    }

    public void clear() {
        scripts.clear();
    }

    public List<DiagnosticMessage> update(String oldName, String newName, String code) throws IllegalArgumentException {
        if (!oldName.equals(newName)) {
            if (exists(newName)) {
                throw new IllegalArgumentException("Script with the same name already exists.");
            }
        }

        if (!exists(oldName)) {
            throw new IllegalArgumentException("Cannot find original script by name " + oldName + ".");
        }

        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Code is required.");
        }

        int bindingIndex = -1;
        String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < KeyBindingsConfig.KeysCount; i++) {
            if (bindings[i] != null && bindings[i].equals(oldName)) {
                bindingIndex = i;
                KeyBindingsController.instance.assign(-1, oldName);
                break;
            }
        }

        Script script = scripts.stream().filter(s -> s.name.equals(oldName)).findFirst().orElse(null);
        ScriptsConfig.ScriptEntry configScript = ConfigStore.instance.getConfig().scriptsConfig.scripts.stream().filter(s -> s.name.equals(oldName)).findFirst().orElse(null);
        if (script != null) {
            script.name = newName;
            script.code = code;
            if (configScript != null) {
                configScript.name = newName;
                configScript.code = code;
            }
            script.compiled = null;
            if (bindingIndex >= 0) {
                bindings[bindingIndex] = newName;
            }
            CompilationResult<Runnable> result = handleKeybindingsCompiler.compile(code, Runnable.class);
            if (result.program() != null) {
                script.compiled = result.program();
                if (bindingIndex >= 0) {
                    KeyBindingsController.instance.assign(bindingIndex, newName);
                }
                return List.of();
            } else {
                return result.diagnostics();
            }
        }

        return List.of();
    }

    public Runnable get(String name) {
        var optional = scripts.stream().filter(s -> s.name.equals(name)).findFirst();
        if (optional.isEmpty()) {
            return null;
        }
        return optional.get().compiled;
    }

    public List<Script> list() {
        return new ArrayList<>(scripts);
    }

    public boolean exists(String name) {
        return scripts.stream().anyMatch(s -> s.name.equals(name));
    }

    public void remove(String name) {
        KeyBindingsController.instance.assign(-1, name);
        scripts.removeIf(s -> s.name.equals(name));
        ConfigStore.instance.getConfig().scriptsConfig.scripts.removeIf(s -> s.name.equals(name));
    }

    public CompilationResult<Runnable> compileOverlay(String code) {
        return overlayCompiler.compile(code, Runnable.class);
    }

    public CompilationResult<Runnable> compileKeys(String code) {
        return handleKeybindingsCompiler.compile(code, Runnable.class);
    }

    public CompilationResult<Runnable> compileBlockAutomation(String code) {
        return blockAutomationCompiler.compile(code, Runnable.class);
    }

    public CompilationResult<Runnable> compileVillagerRoller(String code) {
        return villagerRollerCompiler.compile(code, Runnable.class);
    }

    public CompilationResult<Runnable> compileEvents(String code) {
        return eventsCompiler.compile(code, Runnable.class);
    }

    public CompilationResult<Runnable> compileEntityEsp(String code) {
        return entityEspCompiler.compile(code, Runnable.class);
    }

    public static class Script {
        public String name;
        public String code;
        public Runnable compiled;
    }
}