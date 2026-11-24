package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import net.minecraft.world.item.Item;

public record AutoCraftConfig(
        boolean enabled,
        ImmutableList<Item> items
) implements ImmutableConfig, ToggleableConfig<AutoCraftConfig> {

    public AutoCraftConfig {
        if (items == null) {
            items = ImmutableList.empty();
        }
    }

    public static AutoCraftConfig defaults() {
        return new Builder()
                .enabled(false)
                .items(ImmutableList.empty())
                .build();
    }

    // <generated-code>

    public AutoCraftConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AutoCraftConfig withItems(ImmutableList<Item> items) {
        return Builder.from(this).items(items).build();
    }

    public static class Builder {

        private boolean enabled;
        private ImmutableList<Item> items;

        private Builder() {}

        public static Builder from(AutoCraftConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .items(config.items);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder items(ImmutableList<Item> items) {
            this.items = items;
            return this;
        }

        public AutoCraftConfig build() {
            return new AutoCraftConfig(enabled, items);
        }
    }

    // </generated-code>
}