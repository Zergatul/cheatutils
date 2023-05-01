package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ReachConfig;
import net.minecraft.world.entity.Entity;

public class AttackRange {

    public static double get() {
        ReachConfig config = ConfigStore.instance.getConfig().reachConfig;
        if (config.overrideAttackRange) {
            return config.attackRange;
        } else {
            return 3;
        }
    }

    public static boolean canHit(Entity entity) {
        return true;
    }
}