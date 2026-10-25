package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;
import net.minecraft.world.item.Item;

public record AutoDropConfig(
        ImmutableList<Item> items
) implements ImmutableConfig, ModuleStateProvider {

    public AutoDropConfig {
        if (items == null) {
            items = ImmutableList.empty();
        }
    }

    public static AutoDropConfig defaults() {
        return new Builder()
                .items(ImmutableList.empty())
                .build();
    }

    @Override
    public boolean isEnabled() {
        return !items.isEmpty();
    }

    // <generated-code>

    public AutoDropConfig withItems(ImmutableList<Item> items) {
        return Builder.from(this).items(items).build();
    }

    public static class Builder {

        private ImmutableList<Item> items;

        private Builder() {}

        public static Builder from(AutoDropConfig config) {
            return new Builder()
                    .items(config.items);
        }

        public Builder items(ImmutableList<Item> items) {
            this.items = items;
            return this;
        }

        public AutoDropConfig build() {
            return new AutoDropConfig(items);
        }
    }

    // </generated-code>
}