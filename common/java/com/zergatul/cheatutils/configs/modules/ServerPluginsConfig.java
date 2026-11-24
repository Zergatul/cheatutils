package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record ServerPluginsConfig(
        int waitTicks,
        boolean autoPrint
) implements ImmutableConfig {

    public ServerPluginsConfig {
        waitTicks = MathUtils.clamp(waitTicks, 0, 60 * 20);
    }

    public static ServerPluginsConfig defaults() {
        return new Builder()
                .waitTicks(20)
                .autoPrint(false)
                .build();
    }

    // <generated-code>

    public ServerPluginsConfig withWaitTicks(int waitTicks) {
        return Builder.from(this).waitTicks(waitTicks).build();
    }

    public ServerPluginsConfig withAutoPrint(boolean autoPrint) {
        return Builder.from(this).autoPrint(autoPrint).build();
    }

    public static class Builder {

        private int waitTicks;
        private boolean autoPrint;

        private Builder() {}

        public static Builder from(ServerPluginsConfig config) {
            return new Builder()
                    .waitTicks(config.waitTicks)
                    .autoPrint(config.autoPrint);
        }

        public Builder waitTicks(int waitTicks) {
            this.waitTicks = waitTicks;
            return this;
        }

        public Builder autoPrint(boolean autoPrint) {
            this.autoPrint = autoPrint;
            return this;
        }

        public ServerPluginsConfig build() {
            return new ServerPluginsConfig(waitTicks, autoPrint);
        }
    }

    // </generated-code>
}