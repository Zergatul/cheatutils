package com.zergatul.cheatutils.common;

import java.util.Objects;

public class ModLoaderBridgeInstance {

    private static ModLoaderBridge instance;

    private ModLoaderBridgeInstance() {}

    public static synchronized void init(ModLoaderBridge bridge) {
        if (instance != null) {
            throw new IllegalStateException("Mod-loader bridge is already initialized.");
        }
        instance = Objects.requireNonNull(bridge);
    }

    public static synchronized ModLoaderBridge get() {
        return Objects.requireNonNull(instance, "Mod-loader bridge is not initialized.");
    }
}