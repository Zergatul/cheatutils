package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;

public class EntitiesConfig {

    public ImmutableList<EntityTracerConfig> configs = new ImmutableList<>();

    public void add(EntityTracerConfig config) {
        configs = configs.add(config);
    }

    public void remove(EntityTracerConfig config) {
        configs = configs.remove(config);
    }
}