package com.zergatul.cheatutils.configs;

public class AimAssistConfig implements ModuleStateProvider {

    public boolean bowAssist;

    @Override
    public boolean isEnabled() {
        return bowAssist;
    }
}