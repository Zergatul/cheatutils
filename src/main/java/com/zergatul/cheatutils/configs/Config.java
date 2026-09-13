package com.zergatul.cheatutils.configs;

public class Config implements Sanitizable {

    public BlocksConfig blocks = new BlocksConfig();
    public EntitiesConfig entities = new EntitiesConfig();
    public CoreConfig coreConfig = new CoreConfig();
    public FreeCamConfig freeCamConfig = new FreeCamConfig();
    public KeyBindingsConfig keyBindingsConfig = new KeyBindingsConfig();
    public KeyBindingScriptsConfig keyBindingScriptsConfig = new KeyBindingScriptsConfig();
    public MonacoEditorConfig monacoEditor = new MonacoEditorConfig();
    public FullBrightConfig fullBrightConfig = new FullBrightConfig();

    @Override
    public void sanitize() {
        if (blocks == null) {
            blocks = new BlocksConfig();
        }
        blocks.sanitize();

        if (entities == null) {
            entities = new EntitiesConfig();
        }
        entities.sanitize();

        if (coreConfig == null) {
            coreConfig = new CoreConfig();
        }
        coreConfig.sanitize();

        if (freeCamConfig == null) {
            freeCamConfig = new FreeCamConfig();
        }
        freeCamConfig.sanitize();

        if (keyBindingsConfig == null) {
            keyBindingsConfig = new KeyBindingsConfig();
        }
        keyBindingsConfig.sanitize();

        if (keyBindingScriptsConfig == null) {
            keyBindingScriptsConfig = new KeyBindingScriptsConfig();
        }
        keyBindingScriptsConfig.sanitize();

        if (monacoEditor == null) {
            monacoEditor = new MonacoEditorConfig();
        }

        if (fullBrightConfig == null) {
            fullBrightConfig = new FullBrightConfig();
        }
    }
}