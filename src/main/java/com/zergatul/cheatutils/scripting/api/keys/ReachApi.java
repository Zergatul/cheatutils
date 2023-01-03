package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ReachConfig;

public class ReachApi {

    public void toggleOverrideReachDistance() {
        ReachConfig config = getConfig();
        config.overrideReachDistance = !config.overrideReachDistance;
        ConfigStore.instance.requestWrite();
    }

    public void toggleOverrideAttackRange() {
        ReachConfig config = getConfig();
        config.overrideAttackRange = !config.overrideAttackRange;
        ConfigStore.instance.requestWrite();
    }

    private ReachConfig getConfig() {
        return ConfigStore.instance.getConfig().reachConfig;
    }
}