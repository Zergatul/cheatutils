package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record FreeCamConfig(
        double acceleration,
        double maxSpeed,
        double slowdownFactor,
        boolean renderHands,
        boolean target,
        boolean spectatorFlight,
        boolean rememberInputState
) implements ImmutableConfig {

    public FreeCamConfig {
        acceleration = MathUtils.clamp(acceleration, 5, 500);
        maxSpeed = MathUtils.clamp(maxSpeed, 5, 500);
        slowdownFactor = MathUtils.clamp(slowdownFactor, 1e-9, 0.5);
    }

    public static FreeCamConfig defaults() {
        return new Builder()
                .acceleration(50)
                .maxSpeed(50)
                .slowdownFactor(0.01)
                .renderHands(true)
                .target(true)
                .spectatorFlight(false)
                .rememberInputState(false)
                .build();
    }

    // <generated-code>

    public FreeCamConfig withAcceleration(double acceleration) {
        return Builder.from(this).acceleration(acceleration).build();
    }

    public FreeCamConfig withMaxSpeed(double maxSpeed) {
        return Builder.from(this).maxSpeed(maxSpeed).build();
    }

    public FreeCamConfig withSlowdownFactor(double slowdownFactor) {
        return Builder.from(this).slowdownFactor(slowdownFactor).build();
    }

    public FreeCamConfig withRenderHands(boolean renderHands) {
        return Builder.from(this).renderHands(renderHands).build();
    }

    public FreeCamConfig withTarget(boolean target) {
        return Builder.from(this).target(target).build();
    }

    public FreeCamConfig withSpectatorFlight(boolean spectatorFlight) {
        return Builder.from(this).spectatorFlight(spectatorFlight).build();
    }

    public FreeCamConfig withRememberInputState(boolean rememberInputState) {
        return Builder.from(this).rememberInputState(rememberInputState).build();
    }

    public static class Builder {

        private double acceleration;
        private double maxSpeed;
        private double slowdownFactor;
        private boolean renderHands;
        private boolean target;
        private boolean spectatorFlight;
        private boolean rememberInputState;

        private Builder() {}

        public static Builder from(FreeCamConfig config) {
            return new Builder()
                    .acceleration(config.acceleration)
                    .maxSpeed(config.maxSpeed)
                    .slowdownFactor(config.slowdownFactor)
                    .renderHands(config.renderHands)
                    .target(config.target)
                    .spectatorFlight(config.spectatorFlight)
                    .rememberInputState(config.rememberInputState);
        }

        public Builder acceleration(double acceleration) {
            this.acceleration = acceleration;
            return this;
        }

        public Builder maxSpeed(double maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }

        public Builder slowdownFactor(double slowdownFactor) {
            this.slowdownFactor = slowdownFactor;
            return this;
        }

        public Builder renderHands(boolean renderHands) {
            this.renderHands = renderHands;
            return this;
        }

        public Builder target(boolean target) {
            this.target = target;
            return this;
        }

        public Builder spectatorFlight(boolean spectatorFlight) {
            this.spectatorFlight = spectatorFlight;
            return this;
        }

        public Builder rememberInputState(boolean rememberInputState) {
            this.rememberInputState = rememberInputState;
            return this;
        }

        public FreeCamConfig build() {
            return new FreeCamConfig(acceleration, maxSpeed, slowdownFactor, renderHands, target, spectatorFlight, rememberInputState);
        }
    }

    // </generated-code>
}