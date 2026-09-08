package com.zergatul.cheatutils.configs;

public class Config implements Sanitizable {
    public CoreConfig coreConfig = new CoreConfig();
    public FreeCamConfig freeCamConfig = new FreeCamConfig();
    public KeyBindingsConfig keyBindingsConfig = new KeyBindingsConfig();
    public KeyBindingScriptsConfig keyBindingScriptsConfig = new KeyBindingScriptsConfig();

    @Override
    public void sanitize() {
        if (coreConfig == null) {
            coreConfig = new CoreConfig();
        }
        coreConfig.sanitize();
        if (freeCamConfig == null) {
            freeCamConfig = new FreeCamConfig();
        }
        freeCamConfig.sanitize();
        if (keyBindingsConfig == null) keyBindingsConfig = new KeyBindingsConfig();
        if (keyBindingScriptsConfig == null) keyBindingScriptsConfig = new KeyBindingScriptsConfig();
        keyBindingsConfig.sanitize();
        keyBindingScriptsConfig.sanitize();
    }
}
