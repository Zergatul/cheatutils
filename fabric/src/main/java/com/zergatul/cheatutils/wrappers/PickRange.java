package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ReachConfig;

public class PickRange {

    public static double get() {
        ReachConfig config = ConfigStore.instance.getConfig().reachConfig;
        if (config.overrideReachDistance) {
            return config.reachDistance;
        } else {
            return 4.5;
        }
    }
}