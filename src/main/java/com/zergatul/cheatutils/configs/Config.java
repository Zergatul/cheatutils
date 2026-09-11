package com.zergatul.cheatutils.configs;

public class Config implements Sanitizable {

    public BlocksConfig blocks = new BlocksConfig();
    public EntitiesConfig entities = new EntitiesConfig();
    public CoreConfig coreConfig = new CoreConfig();
    public FreeCamConfig freeCamConfig = new FreeCamConfig();
    public KeyBindingsConfig keyBindingsConfig = new KeyBindingsConfig();
    public KeyBindingScriptsConfig keyBindingScriptsConfig = new KeyBindingScriptsConfig();
    public MonacoEditorConfig monacoEditor = new MonacoEditorConfig();

    @Override
    public void sanitize() {
        if (blocks == null) {
            blocks = new BlocksConfig();
        }
        if (entities == null) {
            entities = new EntitiesConfig();
        }
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

        blocks.sanitize();
        entities.sanitize();
        coreConfig.sanitize();
        freeCamConfig.sanitize();
        keyBindingsConfig.sanitize();
        keyBindingScriptsConfig.sanitize();
    }
}