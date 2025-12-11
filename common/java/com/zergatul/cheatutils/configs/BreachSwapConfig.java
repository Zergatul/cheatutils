package com.zergatul.cheatutils.configs;

public class BreachSwapConfig extends ModuleConfig implements ValidatableConfig {
    public boolean useAxe;
    public String triggerKey;
    BreachSwapConfig(){
        triggerKey = "Left Button";
        useAxe = false;
    }
    @Override
    public void validate() {
        return;
    }    
}