package com.zergatul.cheatutils.compatibility;

public class IrisMixinPlugin extends OptionalMixinPlugin {

    @Override
    protected String getModName() {
        return "Iris";
    }

    @Override
    protected String getDetectionClassName() {
        return "net.irisshaders.iris.Iris";
    }
}