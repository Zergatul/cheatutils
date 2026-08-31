package com.zergatul.cheatutils.compatibility;

public class SodiumMixinPlugin extends OptionalMixinPlugin {

    @Override
    protected String getDetectionClassName() {
        return "me.jellysquid.mods.sodium.client.SodiumClientMod";
    }

    @Override
    protected String getModName() {
        return "Sodium";
    }
}