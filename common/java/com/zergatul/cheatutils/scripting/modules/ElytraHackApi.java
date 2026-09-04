package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;

import java.util.function.Consumer;

@SuppressWarnings("unused")
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
        update(config -> config.maxSpeed = speed);
    }

    public void setVerticalAcceleration(double acceleration) {
        update(config -> config.vanillaFlyVerticalAcceleration = acceleration);
    }

    public void setHorizontalAcceleration(double acceleration) {
        update(config -> config.vanillaFlyHorizontalAcceleration = acceleration);
    }

    private void update(Consumer<ElytraHackConfig> update) {
        ConfigStore.updateFromApi(config -> config.elytraHackConfig, update);
    }

    @Override
    protected ElytraHackConfig getConfig() {
        return ConfigStore.instance.getConfig().elytraHackConfig;
    }
}