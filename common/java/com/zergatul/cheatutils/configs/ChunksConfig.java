package com.zergatul.cheatutils.configs;

public class ChunksConfig implements ModuleStateProvider {

    public boolean ignoreServerViewDistance;
    public boolean dontUnloadChunks;

    @Override
    public boolean isEnabled() {
        return ignoreServerViewDistance || dontUnloadChunks;
    }
}