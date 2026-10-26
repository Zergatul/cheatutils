package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.ElytraHackConfig;
import com.zergatul.cheatutils.modules.hacks.ElytraFly;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class ElytraHackApi extends SettingHandle2Api<ElytraHackConfig> {

    public ElytraHackApi() {
        super(ElytraFly.instance.getSettings());
    }

    @MethodDescription("Blocks per second.")
    public double getMaxSpeed() {
        return getConfig().maxSpeed();
    }

    @MethodDescription("Blocks per second squared while holding Jump or Sneak.")
    public double getVerticalAcceleration() {
        return getConfig().vanillaFlyVerticalAcceleration();
    }

    @MethodDescription("Blocks per second squared while holding Forward or Backward.")
    public double getHorizontalAcceleration() {
        return getConfig().vanillaFlyHorizontalAcceleration();
    }

    @MethodDescription("Blocks per second.")
    @ApiVisibility(ApiType.UPDATE)
    public void setMaxSpeed(double speed) {
        update(c -> c.withMaxSpeed(speed));
    }

    @MethodDescription("Blocks per second squared while holding Jump or Sneak.")
    @ApiVisibility(ApiType.UPDATE)
    public void setVerticalAcceleration(double acceleration) {
        update(c -> c.withVanillaFlyVerticalAcceleration(acceleration));
    }

    @MethodDescription("Blocks per second squared while holding Forward or Backward.")
    @ApiVisibility(ApiType.UPDATE)
    public void setHorizontalAcceleration(double acceleration) {
        update(c -> c.withVanillaFlyHorizontalAcceleration(acceleration));
    }
}