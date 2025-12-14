package com.zergatul.cheatutils.configs;

public class BreachSwapConfig extends ModuleConfig implements ValidatableConfig {
    public boolean useAxe;
    public String triggerKey;
    public boolean autoHit;
    public boolean breakShield;

    BreachSwapConfig() {
        triggerKey = "Left Button";
        useAxe = false;
        autoHit = false;
        breakShield = false;
    }

    @Override
    public void validate() {
        return;
    }
}