package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record ContainerSummaryConfig(
        boolean enabled,
        boolean showForInventory
) implements ImmutableConfig, ToggleableConfig<ContainerSummaryConfig> {

    public static ContainerSummaryConfig defaults() {
        return new Builder()
                .enabled(false)
                .showForInventory(false)
                .build();
    }

    // <generated-code>

    public ContainerSummaryConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public ContainerSummaryConfig withShowForInventory(boolean showForInventory) {
        return Builder.from(this).showForInventory(showForInventory).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean showForInventory;

        private Builder() {}

        public static Builder from(ContainerSummaryConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .showForInventory(config.showForInventory);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder showForInventory(boolean showForInventory) {
            this.showForInventory = showForInventory;
            return this;
        }

        public ContainerSummaryConfig build() {
            return new ContainerSummaryConfig(enabled, showForInventory);
        }
    }

    // </generated-code>
}