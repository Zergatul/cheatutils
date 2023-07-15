package com.zergatul.cheatutils.configs;

public class ScriptedBlockPlacerConfig extends BlockPlacerConfig implements ValidatableConfig {
    public String code;
    public boolean debugMode;

    @Override
    public void copyTo(ScriptedBlockPlacerConfig other) {
        super.copyTo(other);
        other.debugMode = debugMode;
    }
}