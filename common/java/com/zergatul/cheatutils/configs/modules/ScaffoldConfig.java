package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record ScaffoldConfig(
        boolean enabled,
        double distance,
        boolean replaceBlocksFromInventory,
        boolean attachToAir,
        boolean keepSelectedSlot
) implements ImmutableConfig, ToggleableConfig<ScaffoldConfig> {

    public ScaffoldConfig {
        distance = MathUtils.clamp(distance, 0, 0.5);
    }

    public static ScaffoldConfig defaults() {
        return new Builder()
                .enabled(false)
                .distance(0)
                .replaceBlocksFromInventory(false)
                .attachToAir(false)
                .keepSelectedSlot(false)
                .build();
    }

    // <generated-code>

    public ScaffoldConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public ScaffoldConfig withDistance(double distance) {
        return Builder.from(this).distance(distance).build();
    }

    public ScaffoldConfig withReplaceBlocksFromInventory(boolean replaceBlocksFromInventory) {
        return Builder.from(this).replaceBlocksFromInventory(replaceBlocksFromInventory).build();
    }

    public ScaffoldConfig withAttachToAir(boolean attachToAir) {
        return Builder.from(this).attachToAir(attachToAir).build();
    }

    public ScaffoldConfig withKeepSelectedSlot(boolean keepSelectedSlot) {
        return Builder.from(this).keepSelectedSlot(keepSelectedSlot).build();
    }

    public static class Builder {

        private boolean enabled;
        private double distance;
        private boolean replaceBlocksFromInventory;
        private boolean attachToAir;
        private boolean keepSelectedSlot;

        private Builder() {}

        public static Builder from(ScaffoldConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .distance(config.distance)
                    .replaceBlocksFromInventory(config.replaceBlocksFromInventory)
                    .attachToAir(config.attachToAir)
                    .keepSelectedSlot(config.keepSelectedSlot);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder distance(double distance) {
            this.distance = distance;
            return this;
        }

        public Builder replaceBlocksFromInventory(boolean replaceBlocksFromInventory) {
            this.replaceBlocksFromInventory = replaceBlocksFromInventory;
            return this;
        }

        public Builder attachToAir(boolean attachToAir) {
            this.attachToAir = attachToAir;
            return this;
        }

        public Builder keepSelectedSlot(boolean keepSelectedSlot) {
            this.keepSelectedSlot = keepSelectedSlot;
            return this;
        }

        public ScaffoldConfig build() {
            return new ScaffoldConfig(enabled, distance, replaceBlocksFromInventory, attachToAir, keepSelectedSlot);
        }
    }

    // </generated-code>
}
