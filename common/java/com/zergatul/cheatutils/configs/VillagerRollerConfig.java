package com.zergatul.cheatutils.configs;

public class VillagerRollerConfig implements InteractionConfig {

    public String code;
    public boolean autoRotate;

    public void copyTo(VillagerRollerConfig other) {
        other.autoRotate = autoRotate;
    }

    @Override
    public double getMaxRange() {
        return 4;
    }

    @Override
    public boolean shouldAutoRotate() {
        return autoRotate;
    }
}