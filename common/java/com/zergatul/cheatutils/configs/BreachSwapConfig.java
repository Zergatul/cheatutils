package com.zergatul.cheatutils.configs;

public class BreachSwapConfig extends ModuleConfig implements ValidatableConfig {
    BreachSwapConfig(){
        useAxe = false;
    }
    public boolean useAxe;
    @Override
    public void validate() {
        return;
    }

    
}