package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record AutoHotbarConfig(
        boolean enabled,
        boolean refillSlot1,
        boolean refillSlot2,
        boolean refillSlot3,
        boolean refillSlot4,
        boolean refillSlot5,
        boolean refillSlot6,
        boolean refillSlot7,
        boolean refillSlot8,
        boolean refillSlot9,
        boolean refillSlotOffhand
) implements ImmutableConfig, ToggleableConfig<AutoHotbarConfig> {

    public static AutoHotbarConfig defaults() {
        return new Builder()
                .enabled(false)
                .refillSlot1(false)
                .refillSlot2(false)
                .refillSlot3(false)
                .refillSlot4(false)
                .refillSlot5(false)
                .refillSlot6(false)
                .refillSlot7(false)
                .refillSlot8(false)
                .refillSlot9(false)
                .refillSlotOffhand(false)
                .build();
    }

    public boolean shouldRefill(int slot) {
        return switch (slot) {
            case 0 -> refillSlot1;
            case 1 -> refillSlot2;
            case 2 -> refillSlot3;
            case 3 -> refillSlot4;
            case 4 -> refillSlot5;
            case 5 -> refillSlot6;
            case 6 -> refillSlot7;
            case 7 -> refillSlot8;
            case 8 -> refillSlot9;
            default -> false;
        };
    }

    // <generated-code>

    public AutoHotbarConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AutoHotbarConfig withRefillSlot1(boolean refillSlot1) {
        return Builder.from(this).refillSlot1(refillSlot1).build();
    }

    public AutoHotbarConfig withRefillSlot2(boolean refillSlot2) {
        return Builder.from(this).refillSlot2(refillSlot2).build();
    }

    public AutoHotbarConfig withRefillSlot3(boolean refillSlot3) {
        return Builder.from(this).refillSlot3(refillSlot3).build();
    }

    public AutoHotbarConfig withRefillSlot4(boolean refillSlot4) {
        return Builder.from(this).refillSlot4(refillSlot4).build();
    }

    public AutoHotbarConfig withRefillSlot5(boolean refillSlot5) {
        return Builder.from(this).refillSlot5(refillSlot5).build();
    }

    public AutoHotbarConfig withRefillSlot6(boolean refillSlot6) {
        return Builder.from(this).refillSlot6(refillSlot6).build();
    }

    public AutoHotbarConfig withRefillSlot7(boolean refillSlot7) {
        return Builder.from(this).refillSlot7(refillSlot7).build();
    }

    public AutoHotbarConfig withRefillSlot8(boolean refillSlot8) {
        return Builder.from(this).refillSlot8(refillSlot8).build();
    }

    public AutoHotbarConfig withRefillSlot9(boolean refillSlot9) {
        return Builder.from(this).refillSlot9(refillSlot9).build();
    }

    public AutoHotbarConfig withRefillSlotOffhand(boolean refillSlotOffhand) {
        return Builder.from(this).refillSlotOffhand(refillSlotOffhand).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean refillSlot1;
        private boolean refillSlot2;
        private boolean refillSlot3;
        private boolean refillSlot4;
        private boolean refillSlot5;
        private boolean refillSlot6;
        private boolean refillSlot7;
        private boolean refillSlot8;
        private boolean refillSlot9;
        private boolean refillSlotOffhand;

        private Builder() {}

        public static Builder from(AutoHotbarConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .refillSlot1(config.refillSlot1)
                    .refillSlot2(config.refillSlot2)
                    .refillSlot3(config.refillSlot3)
                    .refillSlot4(config.refillSlot4)
                    .refillSlot5(config.refillSlot5)
                    .refillSlot6(config.refillSlot6)
                    .refillSlot7(config.refillSlot7)
                    .refillSlot8(config.refillSlot8)
                    .refillSlot9(config.refillSlot9)
                    .refillSlotOffhand(config.refillSlotOffhand);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder refillSlot1(boolean refillSlot1) {
            this.refillSlot1 = refillSlot1;
            return this;
        }

        public Builder refillSlot2(boolean refillSlot2) {
            this.refillSlot2 = refillSlot2;
            return this;
        }

        public Builder refillSlot3(boolean refillSlot3) {
            this.refillSlot3 = refillSlot3;
            return this;
        }

        public Builder refillSlot4(boolean refillSlot4) {
            this.refillSlot4 = refillSlot4;
            return this;
        }

        public Builder refillSlot5(boolean refillSlot5) {
            this.refillSlot5 = refillSlot5;
            return this;
        }

        public Builder refillSlot6(boolean refillSlot6) {
            this.refillSlot6 = refillSlot6;
            return this;
        }

        public Builder refillSlot7(boolean refillSlot7) {
            this.refillSlot7 = refillSlot7;
            return this;
        }

        public Builder refillSlot8(boolean refillSlot8) {
            this.refillSlot8 = refillSlot8;
            return this;
        }

        public Builder refillSlot9(boolean refillSlot9) {
            this.refillSlot9 = refillSlot9;
            return this;
        }

        public Builder refillSlotOffhand(boolean refillSlotOffhand) {
            this.refillSlotOffhand = refillSlotOffhand;
            return this;
        }

        public AutoHotbarConfig build() {
            return new AutoHotbarConfig(enabled, refillSlot1, refillSlot2, refillSlot3, refillSlot4, refillSlot5, refillSlot6, refillSlot7, refillSlot8, refillSlot9, refillSlotOffhand);
        }
    }

    // </generated-code>
}