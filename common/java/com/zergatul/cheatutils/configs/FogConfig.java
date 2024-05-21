package com.zergatul.cheatutils.configs;

public class FogConfig extends ModuleConfig {

    public static final String METHOD_SKIP_SETUP_FOG = "SkipSetupFog";
    public static final String METHOD_MODIFY_FOG_DISTANCES = "FogDistanceMod";

    public String method;

    public FogConfig() {
        method = METHOD_MODIFY_FOG_DISTANCES;
    }
}