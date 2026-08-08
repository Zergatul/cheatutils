package com.zergatul.cheatutils.configs;

public class FogConfig implements ModuleStateProvider {

    public static final String METHOD_SKIP_SETUP_FOG = "SkipSetupFog";
    public static final String METHOD_MODIFY_FOG_DISTANCES = "FogDistanceMod";

    public boolean disableFog;
    public String method;

    public FogConfig() {
        method = METHOD_SKIP_SETUP_FOG;
    }

    @Override
    public boolean isEnabled() {
        return disableFog;
    }
}