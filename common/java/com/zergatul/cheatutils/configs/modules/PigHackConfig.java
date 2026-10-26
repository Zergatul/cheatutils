package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record PigHackConfig(
        boolean enabled,
        boolean allowRideWithoutCarrot,
        boolean overrideSteeringSpeed,
        float steeringSpeed
) implements ImmutableConfig, ToggleableConfig<PigHackConfig> {

    public PigHackConfig {
        steeringSpeed = MathUtils.clamp(steeringSpeed, 0.01f, 5f);
    }

    public static PigHackConfig defaults() {
        return new Builder()
                .enabled(false)
                .allowRideWithoutCarrot(true)
                .overrideSteeringSpeed(true)
                .steeringSpeed(0.1f)
                .build();
    }

    // <generated-code>

    public PigHackConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public PigHackConfig withAllowRideWithoutCarrot(boolean allowRideWithoutCarrot) {
        return Builder.from(this).allowRideWithoutCarrot(allowRideWithoutCarrot).build();
    }

    public PigHackConfig withOverrideSteeringSpeed(boolean overrideSteeringSpeed) {
        return Builder.from(this).overrideSteeringSpeed(overrideSteeringSpeed).build();
    }

    public PigHackConfig withSteeringSpeed(float steeringSpeed) {
        return Builder.from(this).steeringSpeed(steeringSpeed).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean allowRideWithoutCarrot;
        private boolean overrideSteeringSpeed;
        private float steeringSpeed;

        private Builder() {}

        public static Builder from(PigHackConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .allowRideWithoutCarrot(config.allowRideWithoutCarrot)
                    .overrideSteeringSpeed(config.overrideSteeringSpeed)
                    .steeringSpeed(config.steeringSpeed);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder allowRideWithoutCarrot(boolean allowRideWithoutCarrot) {
            this.allowRideWithoutCarrot = allowRideWithoutCarrot;
            return this;
        }

        public Builder overrideSteeringSpeed(boolean overrideSteeringSpeed) {
            this.overrideSteeringSpeed = overrideSteeringSpeed;
            return this;
        }

        public Builder steeringSpeed(float steeringSpeed) {
            this.steeringSpeed = steeringSpeed;
            return this;
        }

        public PigHackConfig build() {
            return new PigHackConfig(enabled, allowRideWithoutCarrot, overrideSteeringSpeed, steeringSpeed);
        }
    }

    // </generated-code>
}
