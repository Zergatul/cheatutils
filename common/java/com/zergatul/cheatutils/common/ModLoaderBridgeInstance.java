package com.zergatul.cheatutils.common;

public class ModLoaderBridgeInstance {

    private static ModLoaderBridge instance;

    public static void init(ModLoaderBridge bridge) {
        instance = bridge;
    }

    public static ModLoaderBridge get() {
        return instance;
    }
}