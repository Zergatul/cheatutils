package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ElytraHackApi extends ModuleApi<ElytraHackConfig> {

    @MethodDescription("Blocks per second.")
    public double getMaxSpeed() {
        return getConfig().maxSpeed;
    }

    @MethodDescription("Blocks per second squared while holding Jump or Sneak.")
    public double getVerticalAcceleration() {
        return getConfig().vanillaFlyVerticalAcceleration;
    }

    @MethodDescription("Blocks per second squared while holding Forward or Backward.")
    public double getHorizontalAcceleration() {
        return getConfig().vanillaFlyHorizontalAcceleration;
    }

    @MethodDescription("Blocks per second.")
    @ApiVisibility(ApiType.UPDATE)
    public void setMaxSpeed(double speed) {
        update(c -> c.maxSpeed = speed);
    }

    @MethodDescription("Blocks per second squared while holding Jump or Sneak.")
    @ApiVisibility(ApiType.UPDATE)
    public void setVerticalAcceleration(double acceleration) {
        update(c -> c.vanillaFlyVerticalAcceleration = acceleration);
    }

    @MethodDescription("Blocks per second squared while holding Forward or Backward.")
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