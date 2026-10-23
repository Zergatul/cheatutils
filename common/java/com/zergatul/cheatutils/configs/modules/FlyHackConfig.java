package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

@SuppressWarnings("unused")
public record FlyHackConfig(
        boolean enabled,
        boolean overrideFlyingSpeed,
        float flyingSpeed,
        boolean onGroundFlag,
        boolean vanillaAntiFlyBypass,
        int antiFlyInterval,
        double antiFlyDistance
) implements ImmutableConfig, ToggleableConfig<FlyHackConfig> {

    public FlyHackConfig {
        flyingSpeed = MathUtils.clamp(flyingSpeed, 0.001f, 10f);
        antiFlyInterval = MathUtils.clamp(antiFlyInterval, 2, 200);
        antiFlyDistance = MathUtils.clamp(antiFlyDistance, 0.001, 1);
    }

    public static FlyHackConfig defaults() {
        return new Builder()
                .enabled(false)
                .overrideFlyingSpeed(false)
                .flyingSpeed(0.05f)
                .onGroundFlag(false)
                .vanillaAntiFlyBypass(false)
                .antiFlyInterval(40)
                .antiFlyDistance(0.05)
                .build();
    }

    // <generated-code>

    public FlyHackConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public FlyHackConfig withOverrideFlyingSpeed(boolean overrideFlyingSpeed) {
        return Builder.from(this).overrideFlyingSpeed(overrideFlyingSpeed).build();
    }

    public FlyHackConfig withFlyingSpeed(float flyingSpeed) {
        return Builder.from(this).flyingSpeed(flyingSpeed).build();
    }

    public FlyHackConfig withOnGroundFlag(boolean onGroundFlag) {
        return Builder.from(this).onGroundFlag(onGroundFlag).build();
    }

    public FlyHackConfig withVanillaAntiFlyBypass(boolean vanillaAntiFlyBypass) {
        return Builder.from(this).vanillaAntiFlyBypass(vanillaAntiFlyBypass).build();
    }

    public FlyHackConfig withAntiFlyInterval(int antiFlyInterval) {
        return Builder.from(this).antiFlyInterval(antiFlyInterval).build();
    }

    public FlyHackConfig withAntiFlyDistance(double antiFlyDistance) {
        return Builder.from(this).antiFlyDistance(antiFlyDistance).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean overrideFlyingSpeed;
        private float flyingSpeed;
        private boolean onGroundFlag;
        private boolean vanillaAntiFlyBypass;
        private int antiFlyInterval;
        private double antiFlyDistance;

        private Builder() {}

        public static Builder from(FlyHackConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .overrideFlyingSpeed(config.overrideFlyingSpeed)
                    .flyingSpeed(config.flyingSpeed)
                    .onGroundFlag(config.onGroundFlag)
                    .vanillaAntiFlyBypass(config.vanillaAntiFlyBypass)
                    .antiFlyInterval(config.antiFlyInterval)
                    .antiFlyDistance(config.antiFlyDistance);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder overrideFlyingSpeed(boolean overrideFlyingSpeed) {
            this.overrideFlyingSpeed = overrideFlyingSpeed;
            return this;
        }

        public Builder flyingSpeed(float flyingSpeed) {
            this.flyingSpeed = flyingSpeed;
            return this;
        }

        public Builder onGroundFlag(boolean onGroundFlag) {
            this.onGroundFlag = onGroundFlag;
            return this;
        }

        public Builder vanillaAntiFlyBypass(boolean vanillaAntiFlyBypass) {
            this.vanillaAntiFlyBypass = vanillaAntiFlyBypass;
            return this;
        }

        public Builder antiFlyInterval(int antiFlyInterval) {
            this.antiFlyInterval = antiFlyInterval;
            return this;
        }

        public Builder antiFlyDistance(double antiFlyDistance) {
            this.antiFlyDistance = antiFlyDistance;
            return this;
        }

        public FlyHackConfig build() {
            return new FlyHackConfig(enabled, overrideFlyingSpeed, flyingSpeed, onGroundFlag, vanillaAntiFlyBypass, antiFlyInterval, antiFlyDistance);
        }
    }

    // </generated-code>
}