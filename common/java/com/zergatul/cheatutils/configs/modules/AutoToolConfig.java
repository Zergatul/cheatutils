package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record AutoToolConfig(
        boolean enabled,
        String mode,
        String priority,
        int slot,
        int minDurability
) implements ImmutableConfig, ToggleableConfig<AutoToolConfig> {

    public static final String MODE_HOTBAR = "HOTBAR";
    public static final String MODE_INVENTORY = "INVENTORY";

    public static final String PRIORITY_SPEED = "SPEED";
    public static final String PRIORITY_DROP = "DROP";

    public AutoToolConfig {
        if (!MODE_HOTBAR.equals(mode) && !MODE_INVENTORY.equals(mode)) {
            mode = MODE_HOTBAR;
        }
        if (!PRIORITY_SPEED.equals(priority) && !PRIORITY_DROP.equals(priority)) {
            priority = PRIORITY_DROP;
        }
        slot = MathUtils.clamp(slot, 1, 9);
        minDurability = MathUtils.clamp(minDurability, 0, 1000);
    }

    public static AutoToolConfig defaults() {
        return new Builder()
                .enabled(false)
                .mode(MODE_HOTBAR)
                .priority(PRIORITY_DROP)
                .slot(1)
                .minDurability(10)
                .build();
    }

    // <generated-code>

    public AutoToolConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AutoToolConfig withMode(String mode) {
        return Builder.from(this).mode(mode).build();
    }

    public AutoToolConfig withPriority(String priority) {
        return Builder.from(this).priority(priority).build();
    }

    public AutoToolConfig withSlot(int slot) {
        return Builder.from(this).slot(slot).build();
    }

    public AutoToolConfig withMinDurability(int minDurability) {
        return Builder.from(this).minDurability(minDurability).build();
    }

    public static class Builder {

        private boolean enabled;
        private String mode;
        private String priority;
        private int slot;
        private int minDurability;

        private Builder() {}

        public static Builder from(AutoToolConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .mode(config.mode)
                    .priority(config.priority)
                    .slot(config.slot)
                    .minDurability(config.minDurability);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public Builder slot(int slot) {
            this.slot = slot;
            return this;
        }

        public Builder minDurability(int minDurability) {
            this.minDurability = minDurability;
            return this;
        }

        public AutoToolConfig build() {
            return new AutoToolConfig(enabled, mode, priority, slot, minDurability);
        }
    }

    // </generated-code>
}