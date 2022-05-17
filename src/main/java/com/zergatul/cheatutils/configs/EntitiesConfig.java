package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.List;

public class EntitiesConfig {

    public List<EntityTracerConfig> configs = new ArrayList<>();

    public void add(EntityTracerConfig config) {
        synchronized (configs) {
            configs.add(config);
        }
    }

    public void remove(EntityTracerConfig config) {
        synchronized (configs) {
            configs.remove(config);
        }
    }
}
