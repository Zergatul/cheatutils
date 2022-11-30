package com.zergatul.cheatutils.configs;

public class BoatHackConfig implements ValidatableConfig {
    public boolean overrideFriction;
    public float friction;

    public BoatHackConfig() {
        overrideFriction = false;
        friction = 0.9f;
    }

    @Override
    public void validate() {
        friction = Math.min(Math.max(0.01f, friction), 0.99f);
    }
}