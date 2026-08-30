package com.zergatul.cheatutils.compatibility;

public class SodiumMixinPlugin extends OptionalMixinPlugin {

    @Override
    protected String getDetectionClassName() {
        return "net.caffeinemc.mods.sodium.client.SodiumClientMod";
    }

    @Override
    protected String getModName() {
        return "Sodium";
    }
}