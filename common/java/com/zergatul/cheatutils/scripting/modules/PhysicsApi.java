package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PhysicsConfig;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class PhysicsApi extends ModuleApi<PhysicsConfig> {

    @Override
    protected PhysicsConfig getConfig() {
        return ConfigStore.instance.getConfig().physicsConfig;
    }

    // === Métodos personalizados para scripting ===

    @MethodDescription("Gets player friction value")
    public double getPlayerFriction() {
        return getConfig().playerFriction;
    }

    @MethodDescription("Sets player friction value")
    @ApiVisibility(ApiType.UPDATE)
    public void setPlayerFriction(double value) {
        getConfig().playerFriction = value;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Gets boat friction value")
    public double getBoatFriction() {
        return getConfig().boatFriction;
    }

    @MethodDescription("Sets boat friction value")
    @ApiVisibility(ApiType.UPDATE)
    public void setBoatFriction(double value) {
        getConfig().boatFriction = value;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Checks if player friction override is enabled")
    public boolean isOverridePlayerFriction() {
        return getConfig().overridePlayerFriction;
    }

    @MethodDescription("Sets player friction override")
    @ApiVisibility(ApiType.UPDATE)
    public void setOverridePlayerFriction(boolean value) {
        getConfig().overridePlayerFriction = value;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Checks if boat friction override is enabled")
    public boolean isOverrideBoatFriction() {
        return getConfig().overrideBoatFriction;
    }

    @MethodDescription("Sets boat friction override")
    @ApiVisibility(ApiType.UPDATE)
    public void setOverrideBoatFriction(boolean value) {
        getConfig().overrideBoatFriction = value;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Checks if boat follows look direction")
    public boolean isFollowLookDirection() {
        return getConfig().followLookDirection;
    }

    @MethodDescription("Sets boat follow look direction")
    @ApiVisibility(ApiType.UPDATE)
    public void setFollowLookDirection(boolean value) {
        getConfig().followLookDirection = value;
        ConfigStore.instance.requestWrite();
    }

    @MethodDescription("Gets look direction speed")
    public double getLookSpeed() {
        return getConfig().lookSpeed;
    }

    @MethodDescription("Sets look direction speed")
    @ApiVisibility(ApiType.UPDATE)
    public void setLookSpeed(double value) {
        getConfig().lookSpeed = value;
        ConfigStore.instance.requestWrite();
    }

    public boolean isPassengerControls() {
        return getConfig().passengerControls;
    }

    public void setPassengerControls(boolean passengerControls) {
        getConfig().passengerControls = passengerControls;
        ConfigStore.instance.requestWrite();
    }
}