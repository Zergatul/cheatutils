package com.zergatul.cheatutils.configs;

import java.util.List;

public class JsonKillAuraConfig {

    public boolean active;
    public float maxRange;
    public List<String> priorities;

    public KillAuraConfig convert() {
        var config = KillAuraConfig.createDefault();
        config.active = active;
        config.maxRange = maxRange;
        return config;
    }
}
