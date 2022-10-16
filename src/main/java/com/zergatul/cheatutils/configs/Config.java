package com.zergatul.cheatutils.configs;

public class Config {
    public boolean esp;
    public FullBrightConfig fullBrightConfig = new FullBrightConfig();
    public BlocksConfig blocks = new BlocksConfig();
    public EntitiesConfig entities = new EntitiesConfig();
    public FreeCamConfig freeCamConfig = new FreeCamConfig();
    public ScriptsConfig scriptsConfig = new ScriptsConfig();
    public KeyBindingsConfig keyBindingsConfig = new KeyBindingsConfig();

    public Config() {
        esp = true;
    }
}