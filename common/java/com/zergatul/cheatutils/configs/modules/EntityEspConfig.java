package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;

import java.util.Objects;

public record EntityEspConfig(
        ImmutableList<EntityEspEntry> entries
) implements ImmutableConfig, ModuleStateProvider {

    public EntityEspConfig {
        if (entries == null) {
            entries = ImmutableList.empty();
        }

        entries = entries
                .removeAll(Objects::isNull)
                .removeAll(e -> e.type() == null)
                .distinctBy(EntityEspEntry::type);
    }

    public static EntityEspConfig defaults() {
        return new Builder()
                .entries(ImmutableList.empty())
                .build();
    }

    @Override
    public boolean isEnabled() {
        return entries.stream().anyMatch(EntityEspEntry::enabled);
    }

    // <generated-code>

    public EntityEspConfig withEntries(ImmutableList<EntityEspEntry> entries) {
        return Builder.from(this).entries(entries).build();
    }

    public static class Builder {

        private ImmutableList<EntityEspEntry> entries;

        private Builder() {}

        public static Builder from(EntityEspConfig config) {
            return new Builder()
                    .entries(config.entries);
        }

        public Builder entries(ImmutableList<EntityEspEntry> entries) {
            this.entries = entries;
            return this;
        }

        public EntityEspConfig build() {
            return new EntityEspConfig(entries);
        }
    }

    // </generated-code>
}