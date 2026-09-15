package com.zergatul.cheatutils.common;

public interface LoaderBridge {

    LoaderBridge INSTANCE = create();

    LoaderEnvironment getEnvironment();
    LoaderRenderingWorkarounds getRenderingWorkarounds();

    private static LoaderBridge create() {
        // this is getting patched from individual mod loader mixin
        throw new AssertionError();
    }
}