package com.zergatul.cheatutils.common;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface LoaderBridge {

    LoaderBridge INSTANCE = create();

    LoaderEnvironment getEnvironment();
    LoaderRenderingWorkarounds getRenderingWorkarounds();
    @Nullable LoaderInputsWorkarounds getInputsWorkarounds();

    private static LoaderBridge create() {
        // this is getting patched from individual mod loader mixin
        throw new AssertionError();
    }
}