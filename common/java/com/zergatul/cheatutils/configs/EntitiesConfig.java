package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;

public class EntitiesConfig {

    public ImmutableList<EntityEspConfig> configs = new ImmutableList<>();

    public void add(EntityEspConfig config) {
        configs = configs.add(config);
    }

    public void remove(EntityEspConfig config) {
        configs = configs.remove(config);
    }
}