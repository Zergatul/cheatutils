package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingScriptsConfig.ScriptEntry;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import com.zergatul.scripting.DiagnosticMessage;
import java.util.*;

public class KeyBindingScriptsApi extends ApiBase {
    public String getRoute() { return "keybinding-scripts"; }
    public String get() {
        List<Script> list = new ArrayList<>();
        for (ScriptEntry entry : KeyBindings.instance.list()) list.add(new Script(entry));
        return gson.toJson(list);
    }
    public String get(String name) {
        ScriptEntry entry = KeyBindings.instance.get(name);
        if (entry == null) throw new IllegalArgumentException("Script does not exist.");
        return gson.toJson(new Script(entry));
    }
    public String post(String body) {
        ScriptEntry entry = parse(body);
        return result(KeyBindings.instance.add(entry.name, entry.code));
    }
    public String put(String name, String body) {
        ScriptEntry entry = parse(body);
        return result(KeyBindings.instance.update(name, entry.name, entry.code));
    }
    public String delete(String name) {
        KeyBindings.instance.remove(name);
        return "true";
    }
    private ScriptEntry parse(String body) {
        ScriptEntry entry = gson.fromJson(body, ScriptEntry.class);
        if (entry == null) throw new IllegalArgumentException("Script is required.");
        return entry;
    }
    private String result(List<DiagnosticMessage> messages) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ok", messages.isEmpty());
        List<ScriptApi.Diagnostic> diagnostics = new ArrayList<>();
        for (DiagnosticMessage message : messages) diagnostics.add(new ScriptApi.Diagnostic(message));
        response.put("diagnostics", diagnostics);
        return gson.toJson(response);
    }
    private static class Script {
        String name, code, error;
        int key;
        Script(ScriptEntry entry) {
            name = entry.name;
            code = entry.code;
            error = KeyBindings.instance.getError(name);
            key = Arrays.asList(ConfigStore.instance.getConfig().keyBindingsConfig.bindings).indexOf(name);
        }
    }
}
