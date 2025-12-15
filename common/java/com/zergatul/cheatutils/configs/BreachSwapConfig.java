package com.zergatul.cheatutils.configs;

public class BreachSwapConfig extends ModuleConfig implements ValidatableConfig {
    public boolean useAxe;
    public boolean breakShield;

    BreachSwapConfig() {
        useAxe = false;
        breakShield = false;
    }

    @Override
    public void validate() {
        return;
    }
}