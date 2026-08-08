package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;

import java.util.Objects;

public class EntitiesConfig implements ModuleStateProvider, Sanitizable {

    public ImmutableList<EntityTracerConfig> configs = new ImmutableList<>();

    public void add(EntityTracerConfig config) {
        configs = configs.add(config);
    }

    public void remove(EntityTracerConfig config) {
        configs = configs.remove(config);
    }

    @Override
    public boolean isEnabled() {
        return configs.stream().anyMatch(c -> c.enabled);
    }

    @Override
    public void sanitize() {
        // clazz==null can occur after removing mod with custom entities
        configs = configs
                .removeIf(Objects::isNull)
                .removeIf(c -> c.clazz == null);
    }
}