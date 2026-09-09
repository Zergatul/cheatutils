package com.zergatul.cheatutils.configs;

public class Config implements Sanitizable {

    public CoreConfig coreConfig = new CoreConfig();
    public FreeCamConfig freeCamConfig = new FreeCamConfig();
    public KeyBindingsConfig keyBindingsConfig = new KeyBindingsConfig();
    public KeyBindingScriptsConfig keyBindingScriptsConfig = new KeyBindingScriptsConfig();
    public MonacoEditorConfig monacoEditor = new MonacoEditorConfig();

    @Override
    public void sanitize() {
        if (coreConfig == null) {
            coreConfig = new CoreConfig();
        }
        if (freeCamConfig == null) {
            freeCamConfig = new FreeCamConfig();
        }
        if (keyBindingsConfig == null) {
            keyBindingsConfig = new KeyBindingsConfig();
        }
        if (keyBindingScriptsConfig == null) {
            keyBindingScriptsConfig = new KeyBindingScriptsConfig();
        }
        if (monacoEditor == null) {
            monacoEditor = new MonacoEditorConfig();
        }

        coreConfig.sanitize();
        freeCamConfig.sanitize();
        keyBindingsConfig.sanitize();
        keyBindingScriptsConfig.sanitize();
    }
}