package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record ElytraBounceConfig(
        int firstJumpTicks,
        int betweenJumpsTicks,
        int secondJumpTicks
) implements ImmutableConfig {

    public ElytraBounceConfig {
        firstJumpTicks = MathUtils.clamp(firstJumpTicks, 1, 20);
        betweenJumpsTicks = MathUtils.clamp(betweenJumpsTicks, 1, 20);
        secondJumpTicks = MathUtils.clamp(secondJumpTicks, 1, 20);
    }

    public static ElytraBounceConfig defaults() {
        return new Builder()
                .firstJumpTicks(1)
                .betweenJumpsTicks(1)
                .secondJumpTicks(1)
                .build();
    }

    // <generated-code>

    public ElytraBounceConfig withFirstJumpTicks(int firstJumpTicks) {
        return Builder.from(this).firstJumpTicks(firstJumpTicks).build();
    }

    public ElytraBounceConfig withBetweenJumpsTicks(int betweenJumpsTicks) {
        return Builder.from(this).betweenJumpsTicks(betweenJumpsTicks).build();
    }

    public ElytraBounceConfig withSecondJumpTicks(int secondJumpTicks) {
        return Builder.from(this).secondJumpTicks(secondJumpTicks).build();
    }

    public static class Builder {

        private int firstJumpTicks;
        private int betweenJumpsTicks;
        private int secondJumpTicks;

        private Builder() {}

        public static Builder from(ElytraBounceConfig config) {
            return new Builder()
                    .firstJumpTicks(config.firstJumpTicks)
                    .betweenJumpsTicks(config.betweenJumpsTicks)
                    .secondJumpTicks(config.secondJumpTicks);
        }

        public Builder firstJumpTicks(int firstJumpTicks) {
            this.firstJumpTicks = firstJumpTicks;
            return this;
        }

        public Builder betweenJumpsTicks(int betweenJumpsTicks) {
            this.betweenJumpsTicks = betweenJumpsTicks;
            return this;
        }

        public Builder secondJumpTicks(int secondJumpTicks) {
            this.secondJumpTicks = secondJumpTicks;
            return this;
        }

        public ElytraBounceConfig build() {
            return new ElytraBounceConfig(firstJumpTicks, betweenJumpsTicks, secondJumpTicks);
        }
    }

    // </generated-code>
}