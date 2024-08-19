package com.zergatul.cheatutils.configs;

public class AdvancedTooltipsConfig implements ModuleStateProvider {

    public boolean beeContainer;
    public boolean repairCost;

    @Override
    public boolean isEnabled() {
        return beeContainer || repairCost;
    }
}
