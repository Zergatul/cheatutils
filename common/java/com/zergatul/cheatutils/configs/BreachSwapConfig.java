package com.zergatul.cheatutils.configs;
import com.zergatul.cheatutils.scripting.Root;
public class BreachSwapConfig extends ModuleConfig implements ValidatableConfig {
    
    public boolean useAxe;
    public String triggerKey;

    BreachSwapConfig(){
        useAxe = false;
        triggerKey = "Left Button";
    }
    @Override
    public void validate() {
        useAxe = useAxe ? true : false;
        if(!(Root.input.isValidKey(triggerKey))){
            triggerKey = "Left Button";
        }
    }    
}