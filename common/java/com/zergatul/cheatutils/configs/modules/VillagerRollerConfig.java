package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.InteractionConfig;
import com.zergatul.cheatutils.configs.ImmutableConfigWithUpdateOptions;

public record VillagerRollerConfig(
        boolean autoRotate,
        String code
) implements ImmutableConfigWithUpdateOptions<VillagerRollerConfig.UpdateOptions>, InteractionConfig {

    public static VillagerRollerConfig defaults() {
        return new Builder()
                .autoRotate(false)
                .code(null)
                .build();
    }

    @Override
    public double getMaxRange() {
        return 4;
    }

    @Override
    public boolean shouldAutoRotate() {
        return autoRotate;
    }

    // <generated-code>

    public VillagerRollerConfig withAutoRotate(boolean autoRotate) {
        return Builder.from(this).autoRotate(autoRotate).build();
    }

    public VillagerRollerConfig withCode(String code) {
        return Builder.from(this).code(code).build();
    }

    @Override
    public VillagerRollerConfig withOptions(UpdateOptions options) {
        return Builder.from(this)
                .autoRotate(options.autoRotate())
                .build();
    }

    public record UpdateOptions(
            boolean autoRotate
    ) {}

    public static class Builder {

        private boolean autoRotate;
        private String code;

        private Builder() {}

        public static Builder from(VillagerRollerConfig config) {
            return new Builder()
                    .autoRotate(config.autoRotate)
                    .code(config.code);
        }

        public Builder autoRotate(boolean autoRotate) {
            this.autoRotate = autoRotate;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public VillagerRollerConfig build() {
            return new VillagerRollerConfig(autoRotate, code);
        }
    }

    // </generated-code>
}