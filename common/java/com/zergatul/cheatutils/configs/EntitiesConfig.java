package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;

public class EntitiesConfig implements ModuleStateProvider, Sanitizable {

    public ImmutableList<EntityEspConfig> configs = new ImmutableList<>();

    public void add(EntityEspConfig config) {
        configs = configs.add(config);
    }

    public void remove(EntityEspConfig config) {
        configs = configs.remove(config);
    }

    @Override
    public boolean isEnabled() {
        return configs.stream().anyMatch(c -> c.enabled);
    }

    @Override
    public void sanitize() {
        // clazz==null can occur after removing mod with custom entities
        configs = configs.removeIf(config -> config.clazz == null);
    }
}