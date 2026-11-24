package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;

public record AdvancedTooltipsConfig(
        boolean beeContainer,
        boolean repairCost
) implements ImmutableConfig, ModuleStateProvider {

    public static AdvancedTooltipsConfig defaults() {
        return new Builder()
                .beeContainer(false)
                .repairCost(false)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return beeContainer || repairCost;
    }

    // <generated-code>

    public AdvancedTooltipsConfig withBeeContainer(boolean beeContainer) {
        return Builder.from(this).beeContainer(beeContainer).build();
    }

    public AdvancedTooltipsConfig withRepairCost(boolean repairCost) {
        return Builder.from(this).repairCost(repairCost).build();
    }

    public static class Builder {

        private boolean beeContainer;
        private boolean repairCost;

        private Builder() {}

        public static Builder from(AdvancedTooltipsConfig config) {
            return new Builder()
                    .beeContainer(config.beeContainer)
                    .repairCost(config.repairCost);
        }

        public Builder beeContainer(boolean beeContainer) {
            this.beeContainer = beeContainer;
            return this;
        }

        public Builder repairCost(boolean repairCost) {
            this.repairCost = repairCost;
            return this;
        }

        public AdvancedTooltipsConfig build() {
            return new AdvancedTooltipsConfig(beeContainer, repairCost);
        }
    }

    // </generated-code>
}