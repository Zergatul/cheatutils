package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyBindingScriptsConfig implements Sanitizable {

    public List<ScriptEntry> scripts = new ArrayList<>();

    public KeyBindingScriptsConfig() {}

    @Override
    public void sanitize() {
        if (scripts == null) {
            scripts = new ArrayList<>();
        }

        Set<String> names = new HashSet<>();
        scripts.removeIf(entry -> {
            if (entry == null) {
                return true;
            }
            if (entry.name == null) {
                return true;
            }
            if (entry.name.trim().isEmpty()) {
                return true;
            }
            if (entry.code == null) {
                return true;
            }
            if (!names.add(entry.name)) {
                return true;
            }
            return false;
        });
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