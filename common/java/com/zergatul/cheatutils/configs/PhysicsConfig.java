package com.zergatul.cheatutils.configs;

public class PhysicsConfig extends ModuleConfig implements Sanitizable {

    // === Fricción existente ===
    public double playerFriction;
    public double boatFriction;
    public boolean overridePlayerFriction;
    public boolean overrideBoatFriction;

    // === NUEVO: Dirección del bote ===
    public boolean followLookDirection;
    public double lookSpeed;
    public boolean verticalMode;
    public double verticalSpeed;
    public boolean passengerControls;


    public PhysicsConfig() {
        playerFriction = 1f;
        boatFriction = 0.6f;
        overrideBoatFriction = false;
        overridePlayerFriction = false;

        passengerControls = false;
        followLookDirection = false;
        lookSpeed = 0.3;
        verticalMode = false;
        verticalSpeed = 0.2;


    }

    @Override
    public void sanitize() {
        if (playerFriction < 0) playerFriction = 0;
        if (boatFriction < 0) boatFriction = 0;
        if (lookSpeed < 0) lookSpeed = 0;
        if (lookSpeed > 2) lookSpeed = 2;
        if (verticalSpeed < 0) verticalSpeed = 0;
        if (verticalSpeed > 1) verticalSpeed = 1;
    }
}