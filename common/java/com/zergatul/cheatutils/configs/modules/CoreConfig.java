package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record CoreConfig(
        int port,
        boolean advancedScripting
) implements ImmutableConfig {

    public CoreConfig {
        port = MathUtils.clamp(port, 1, 65535);
    }

    public static CoreConfig defaults() {
        return new Builder()
                .port(5005)
                .advancedScripting(false)
                .build();
    }

    // <generated-code>

    public CoreConfig withPort(int port) {
        return Builder.from(this).port(port).build();
    }

    public CoreConfig withAdvancedScripting(boolean advancedScripting) {
        return Builder.from(this).advancedScripting(advancedScripting).build();
    }

    public static class Builder {

        private int port;
        private boolean advancedScripting;

        private Builder() {}

        public static Builder from(CoreConfig config) {
            return new Builder()
                    .port(config.port)
                    .advancedScripting(config.advancedScripting);
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder advancedScripting(boolean advancedScripting) {
            this.advancedScripting = advancedScripting;
            return this;
        }

        public CoreConfig build() {
            return new CoreConfig(port, advancedScripting);
        }
    }

    // </generated-code>
}