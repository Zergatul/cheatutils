package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;

public class ElytraHackApi extends ModuleApi<ElytraHackConfig> {

    public double getMaxSpeed() {
        return getConfig().maxSpeed;
    }

    public double getVerticalAcceleration() {
        return getConfig().vanillaFlyVerticalAcceleration;
    }

    public double getHorizontalAcceleration() {
        return getConfig().vanillaFlyHorizontalAcceleration;
    }

    public void setMaxSpeed(double speed) {
        var config = getConfig();
        getConfig().maxSpeed = speed;
        config.validate();
        ConfigStore.instance.requestWrite();
    }

    public void setVerticalAcceleration(double acceleration) {
        var config = getConfig();
        getConfig().vanillaFlyVerticalAcceleration = acceleration;
        config.validate();
        ConfigStore.instance.requestWrite();
    }

    public void setHorizontalAcceleration(double acceleration) {
        var config = getConfig();
        getConfig().vanillaFlyHorizontalAcceleration = acceleration;
        config.validate();
        ConfigStore.instance.requestWrite();
    }

    @Override
    protected ElytraHackConfig getConfig() {
        return ConfigStore.instance.getConfig().elytraHackConfig;
    }
}