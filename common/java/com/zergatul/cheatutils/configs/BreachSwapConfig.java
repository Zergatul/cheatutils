package com.zergatul.cheatutils.configs;

public class BreachSwapConfig extends ModuleConfig implements ValidatableConfig {

    public boolean useAxe;
    @Override
    public void validate() {
        if(useAxe){
            useAxe = true;
        }
        else {
            useAxe = false;
        }
    }
}