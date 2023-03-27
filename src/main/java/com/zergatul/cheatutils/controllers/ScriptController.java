package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.configs.ScriptsConfig;
import com.zergatul.cheatutils.scripting.api.VisibilityCheck;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.cheatutils.scripting.generated.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptController {

    public static final ScriptController instance = new ScriptController();

    private final ScriptingLanguageCompiler handleKeybindingsCompiler = new ScriptingLanguageCompiler(
            com.zergatul.cheatutils.scripting.api.Root.class,
            VisibilityCheck.getTypes("handle-keybindings"));
    private final ScriptingLanguageCompiler overlayCompiler = new ScriptingLanguageCompiler(
            com.zergatul.cheatutils.scripting.api.Root.class,
            VisibilityCheck.getTypes("overlay"));
    private List<Script> scripts = Collections.synchronizedList(new ArrayList<>());

    private ScriptController() {

    }

    public void add(String name, String code) throws IllegalArgumentException, ScriptCompileException, ParseException {
        if (name == null) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (code == null || code.length() == 0) {
            throw new IllegalArgumentException("Code is required.");
        }
        if (exists(name)) {
            throw new IllegalArgumentException("Script with the same name already exists.");
        }
        var script = new Script();
        script.name = name;
        script.code = code;
        script.compiled = handleKeybindingsCompiler.compile(code);
        scripts.add(script);
        ConfigStore.instance.getConfig().scriptsConfig.scripts.add(new ScriptsConfig.ScriptEntry(name, code));
    }

    public void update(String oldName, String newName, String code) throws IllegalArgumentException, ScriptCompileException, ParseException {
        if (!oldName.equals(newName)) {
            if (exists(newName)) {
                throw new IllegalArgumentException("Script with the same name already exists.");
            }
        }

        if (!exists(oldName)) {
            throw new IllegalArgumentException("Cannot find original script by name " + oldName + ".");
        }

        if (code == null || code.length() == 0) {
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
            script.compiled = handleKeybindingsCompiler.compile(code);
            if (bindingIndex >= 0) {
                KeyBindingsController.instance.assign(bindingIndex, newName);
            }
        }
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

    public Runnable compileOverlay(String code) throws ParseException, ScriptCompileException {
        return overlayCompiler.compile(code);
    }

    public static class Script {
        public String name;
        public String code;
        public Runnable compiled;
    }
}