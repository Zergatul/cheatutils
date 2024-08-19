package com.zergatul.cheatutils.configs;

public class LockInputsConfig implements ModuleStateProvider {

    public boolean mouseInputDisabled;
    public boolean holdForward;
    public boolean holdAttack;
    public boolean holdUse;

    @Override
    public boolean isEnabled() {
        return mouseInputDisabled || holdForward || holdAttack || holdUse;
    }
}