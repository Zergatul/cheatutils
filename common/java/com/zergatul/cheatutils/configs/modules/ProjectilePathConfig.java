package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record ProjectilePathConfig(
        boolean enabled,
        boolean enderPearls,
        boolean bows,
        boolean crossbows,
        boolean tridents,
        boolean snowballs,
        boolean potions,
        boolean expBottles,
        boolean eggs,
        boolean showTraces,
        int tracesDuration,
        int fadeDuration
) implements ImmutableConfig, ToggleableConfig<ProjectilePathConfig> {

    public ProjectilePathConfig {
        tracesDuration = MathUtils.clamp(tracesDuration, 0, 3600);
        fadeDuration = MathUtils.clamp(fadeDuration, 0, tracesDuration);
    }

    public static ProjectilePathConfig defaults() {
        return new Builder()
                .enabled(true)
                .enderPearls(true)
                .bows(false)
                .crossbows(false)
                .tridents(false)
                .snowballs(false)
                .potions(false)
                .expBottles(false)
                .eggs(false)
                .showTraces(false)
                .tracesDuration(10)
                .fadeDuration(2)
                .build();
    }

    // <generated-code>

    public ProjectilePathConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public ProjectilePathConfig withEnderPearls(boolean enderPearls) {
        return Builder.from(this).enderPearls(enderPearls).build();
    }

    public ProjectilePathConfig withBows(boolean bows) {
        return Builder.from(this).bows(bows).build();
    }

    public ProjectilePathConfig withCrossbows(boolean crossbows) {
        return Builder.from(this).crossbows(crossbows).build();
    }

    public ProjectilePathConfig withTridents(boolean tridents) {
        return Builder.from(this).tridents(tridents).build();
    }

    public ProjectilePathConfig withSnowballs(boolean snowballs) {
        return Builder.from(this).snowballs(snowballs).build();
    }

    public ProjectilePathConfig withPotions(boolean potions) {
        return Builder.from(this).potions(potions).build();
    }

    public ProjectilePathConfig withExpBottles(boolean expBottles) {
        return Builder.from(this).expBottles(expBottles).build();
    }

    public ProjectilePathConfig withEggs(boolean eggs) {
        return Builder.from(this).eggs(eggs).build();
    }

    public ProjectilePathConfig withShowTraces(boolean showTraces) {
        return Builder.from(this).showTraces(showTraces).build();
    }

    public ProjectilePathConfig withTracesDuration(int tracesDuration) {
        return Builder.from(this).tracesDuration(tracesDuration).build();
    }

    public ProjectilePathConfig withFadeDuration(int fadeDuration) {
        return Builder.from(this).fadeDuration(fadeDuration).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean enderPearls;
        private boolean bows;
        private boolean crossbows;
        private boolean tridents;
        private boolean snowballs;
        private boolean potions;
        private boolean expBottles;
        private boolean eggs;
        private boolean showTraces;
        private int tracesDuration;
        private int fadeDuration;

        private Builder() {}

        public static Builder from(ProjectilePathConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .enderPearls(config.enderPearls)
                    .bows(config.bows)
                    .crossbows(config.crossbows)
                    .tridents(config.tridents)
                    .snowballs(config.snowballs)
                    .potions(config.potions)
                    .expBottles(config.expBottles)
                    .eggs(config.eggs)
                    .showTraces(config.showTraces)
                    .tracesDuration(config.tracesDuration)
                    .fadeDuration(config.fadeDuration);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder enderPearls(boolean enderPearls) {
            this.enderPearls = enderPearls;
            return this;
        }

        public Builder bows(boolean bows) {
            this.bows = bows;
            return this;
        }

        public Builder crossbows(boolean crossbows) {
            this.crossbows = crossbows;
            return this;
        }

        public Builder tridents(boolean tridents) {
            this.tridents = tridents;
            return this;
        }

        public Builder snowballs(boolean snowballs) {
            this.snowballs = snowballs;
            return this;
        }

        public Builder potions(boolean potions) {
            this.potions = potions;
            return this;
        }

        public Builder expBottles(boolean expBottles) {
            this.expBottles = expBottles;
            return this;
        }

        public Builder eggs(boolean eggs) {
            this.eggs = eggs;
            return this;
        }

        public Builder showTraces(boolean showTraces) {
            this.showTraces = showTraces;
            return this;
        }

        public Builder tracesDuration(int tracesDuration) {
            this.tracesDuration = tracesDuration;
            return this;
        }

        public Builder fadeDuration(int fadeDuration) {
            this.fadeDuration = fadeDuration;
            return this;
        }

        public ProjectilePathConfig build() {
            return new ProjectilePathConfig(enabled, enderPearls, bows, crossbows, tridents, snowballs, potions, expBottles, eggs, showTraces, tracesDuration, fadeDuration);
        }
    }

    // </generated-code>
}