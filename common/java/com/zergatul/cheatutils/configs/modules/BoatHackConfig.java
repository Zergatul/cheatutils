package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;
import com.zergatul.cheatutils.utils.MathUtils;

public record BoatHackConfig(
        boolean fly,
        double horizontalFlySpeed,
        double verticalFlySpeed
) implements ImmutableConfig, ModuleStateProvider {

    public BoatHackConfig {
        horizontalFlySpeed = MathUtils.clamp(horizontalFlySpeed, 0, 100);
        verticalFlySpeed = MathUtils.clamp(verticalFlySpeed, 0, 100);
    }

    public static BoatHackConfig defaults() {
        return new Builder()
                .fly(false)
                .horizontalFlySpeed(10)
                .verticalFlySpeed(5)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return fly;
    }

    // <generated-code>

    public BoatHackConfig withFly(boolean fly) {
        return Builder.from(this).fly(fly).build();
    }

    public BoatHackConfig withHorizontalFlySpeed(double horizontalFlySpeed) {
        return Builder.from(this).horizontalFlySpeed(horizontalFlySpeed).build();
    }

    public BoatHackConfig withVerticalFlySpeed(double verticalFlySpeed) {
        return Builder.from(this).verticalFlySpeed(verticalFlySpeed).build();
    }

    public static class Builder {

        private boolean fly;
        private double horizontalFlySpeed;
        private double verticalFlySpeed;

        private Builder() {}

        public static Builder from(BoatHackConfig config) {
            return new Builder()
                    .fly(config.fly)
                    .horizontalFlySpeed(config.horizontalFlySpeed)
                    .verticalFlySpeed(config.verticalFlySpeed);
        }

        public Builder fly(boolean fly) {
            this.fly = fly;
            return this;
        }

        public Builder horizontalFlySpeed(double horizontalFlySpeed) {
            this.horizontalFlySpeed = horizontalFlySpeed;
            return this;
        }

        public Builder verticalFlySpeed(double verticalFlySpeed) {
            this.verticalFlySpeed = verticalFlySpeed;
            return this;
        }

        public BoatHackConfig build() {
            return new BoatHackConfig(fly, horizontalFlySpeed, verticalFlySpeed);
        }
    }

    // </generated-code>
}