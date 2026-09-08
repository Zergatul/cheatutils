package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyBindingScriptsConfig implements Sanitizable {
    public List<ScriptEntry> scripts = new ArrayList<>();

    public KeyBindingScriptsConfig() {
        scripts.add(new ScriptEntry("Toggle ESP", "esp.toggle();"));
        scripts.add(new ScriptEntry("Toggle FreeCam", "freeCam.toggle();"));
    }

    @Override
    public void sanitize() {
        if (scripts == null) scripts = new ArrayList<>();
        Set<String> names = new HashSet<>();
        scripts.removeIf(entry -> entry == null || entry.name == null || entry.name.trim().isEmpty()
                || entry.code == null || !names.add(entry.name));
    }

    public static class ScriptEntry {
        public String name;
        public String code;

        public ScriptEntry(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }
}
