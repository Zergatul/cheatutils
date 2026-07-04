package com.zergatul.cheatutils.modules.esp.entity;

import com.zergatul.cheatutils.configs.EntityEspConfig;

public class EntityScriptResultKey {

    private final int id;
    public final EntityEspConfig config;

    public EntityScriptResultKey(int id, EntityEspConfig config) {
        this.id = id;
        this.config = config;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityScriptResultKey other) {
            return other.id == id && other.config == config;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * id + config.hashCode();
    }
}