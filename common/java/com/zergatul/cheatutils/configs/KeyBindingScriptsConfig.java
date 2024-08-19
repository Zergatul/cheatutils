package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyBindingScriptsConfig implements ModuleStateProvider {

    public List<ScriptEntry> scripts = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isEnabled() {
        return !scripts.isEmpty();
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