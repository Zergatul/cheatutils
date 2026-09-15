package com.zergatul.cheatutils.compatibility;

public class SodiumMixinPlugin extends OptionalMixinPlugin {

    @Override
    protected String getModName() {
        return "Sodium";
    }

    @Override
    protected String getDetectionClassName() {
        return "net.caffeinemc.mods.sodium.client.SodiumClientMod";
    }
}