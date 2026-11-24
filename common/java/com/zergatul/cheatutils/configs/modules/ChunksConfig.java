package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;

public record ChunksConfig(
        boolean ignoreServerViewDistance,
        boolean dontUnloadChunks
) implements ImmutableConfig, ModuleStateProvider {

    public static ChunksConfig defaults() {
        return new Builder()
                .ignoreServerViewDistance(false)
                .dontUnloadChunks(false)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return ignoreServerViewDistance || dontUnloadChunks;
    }

    // <generated-code>

    public ChunksConfig withIgnoreServerViewDistance(boolean ignoreServerViewDistance) {
        return Builder.from(this).ignoreServerViewDistance(ignoreServerViewDistance).build();
    }

    public ChunksConfig withDontUnloadChunks(boolean dontUnloadChunks) {
        return Builder.from(this).dontUnloadChunks(dontUnloadChunks).build();
    }

    public static class Builder {

        private boolean ignoreServerViewDistance;
        private boolean dontUnloadChunks;

        private Builder() {}

        public static Builder from(ChunksConfig config) {
            return new Builder()
                    .ignoreServerViewDistance(config.ignoreServerViewDistance)
                    .dontUnloadChunks(config.dontUnloadChunks);
        }

        public Builder ignoreServerViewDistance(boolean ignoreServerViewDistance) {
            this.ignoreServerViewDistance = ignoreServerViewDistance;
            return this;
        }

        public Builder dontUnloadChunks(boolean dontUnloadChunks) {
            this.dontUnloadChunks = dontUnloadChunks;
            return this;
        }

        public ChunksConfig build() {
            return new ChunksConfig(ignoreServerViewDistance, dontUnloadChunks);
        }
    }

    // </generated-code>
}