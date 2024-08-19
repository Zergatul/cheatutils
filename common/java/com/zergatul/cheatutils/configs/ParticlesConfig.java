package com.zergatul.cheatutils.configs;

public class ParticlesConfig implements ModuleStateProvider {

    public boolean disableBlockBreaking;
    public boolean disableBlockDestroyed;

    @Override
    public boolean isEnabled() {
        return disableBlockBreaking || disableBlockDestroyed;
    }
}