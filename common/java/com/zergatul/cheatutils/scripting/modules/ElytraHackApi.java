package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

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

    @ApiVisibility(ApiType.UPDATE)
    public void setMaxSpeed(double speed) {
        update(c -> c.maxSpeed = speed);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setVerticalAcceleration(double acceleration) {
        update(c -> c.vanillaFlyVerticalAcceleration = acceleration);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setHorizontalAcceleration(double acceleration) {
        update(c -> c.vanillaFlyHorizontalAcceleration = acceleration);
    }

    private void update(Consumer<ElytraHackConfig> update) {
        ConfigStore.updateFromApi(c -> c.elytraHackConfig, update);
    }

    @Override
    protected ElytraHackConfig getConfig() {
        return ConfigStore.instance.getConfig().elytraHackConfig;
    }
}