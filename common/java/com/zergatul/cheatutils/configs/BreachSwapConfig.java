package com.zergatul.cheatutils.configs;

public class BreachSwapConfig extends ModuleConfig implements ValidatableConfig {
    public boolean useAxe;
    public String triggerKey;
    public boolean autoHit;
    BreachSwapConfig(){
        triggerKey = "Left Button";
        useAxe = false;
        autoHit = false;
    }
    @Override
    public void validate() {
        return;
    }    
}