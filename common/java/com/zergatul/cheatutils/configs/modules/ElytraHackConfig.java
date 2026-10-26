package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record ElytraHackConfig(
        boolean enabled,
        String method,
        double vanillaFlyVerticalAcceleration,
        double vanillaFlyHorizontalAcceleration,
        double maxSpeed
) implements ImmutableConfig, ToggleableConfig<ElytraHackConfig> {

    public static final String VANILLA_FLY = "VanillaFly";
    public static final String CREATIVE_FLY = "CreativeFly";

    public ElytraHackConfig {
        maxSpeed = MathUtils.clamp(maxSpeed, 1, 1000);

        vanillaFlyVerticalAcceleration = MathUtils.clamp(vanillaFlyVerticalAcceleration, 0, 50);
        vanillaFlyHorizontalAcceleration = MathUtils.clamp(vanillaFlyHorizontalAcceleration, 0, 50);
    }

    public static ElytraHackConfig defaults() {
        return new Builder()
                .enabled(false)
                .method(VANILLA_FLY)
                .vanillaFlyVerticalAcceleration(4)
                .vanillaFlyHorizontalAcceleration(1)
                .maxSpeed(50)
                .build();
    }

    // <generated-code>

    public ElytraHackConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public ElytraHackConfig withMethod(String method) {
        return Builder.from(this).method(method).build();
    }

    public ElytraHackConfig withVanillaFlyVerticalAcceleration(double vanillaFlyVerticalAcceleration) {
        return Builder.from(this).vanillaFlyVerticalAcceleration(vanillaFlyVerticalAcceleration).build();
    }

    public ElytraHackConfig withVanillaFlyHorizontalAcceleration(double vanillaFlyHorizontalAcceleration) {
        return Builder.from(this).vanillaFlyHorizontalAcceleration(vanillaFlyHorizontalAcceleration).build();
    }

    public ElytraHackConfig withMaxSpeed(double maxSpeed) {
        return Builder.from(this).maxSpeed(maxSpeed).build();
    }

    public static class Builder {

        private boolean enabled;
        private String method;
        private double vanillaFlyVerticalAcceleration;
        private double vanillaFlyHorizontalAcceleration;
        private double maxSpeed;

        private Builder() {}

        public static Builder from(ElytraHackConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .method(config.method)
                    .vanillaFlyVerticalAcceleration(config.vanillaFlyVerticalAcceleration)
                    .vanillaFlyHorizontalAcceleration(config.vanillaFlyHorizontalAcceleration)
                    .maxSpeed(config.maxSpeed);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder vanillaFlyVerticalAcceleration(double vanillaFlyVerticalAcceleration) {
            this.vanillaFlyVerticalAcceleration = vanillaFlyVerticalAcceleration;
            return this;
        }

        public Builder vanillaFlyHorizontalAcceleration(double vanillaFlyHorizontalAcceleration) {
            this.vanillaFlyHorizontalAcceleration = vanillaFlyHorizontalAcceleration;
            return this;
        }

        public Builder maxSpeed(double maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }

        public ElytraHackConfig build() {
            return new ElytraHackConfig(enabled, method, vanillaFlyVerticalAcceleration, vanillaFlyHorizontalAcceleration, maxSpeed);
        }
    }

    // </generated-code>
}