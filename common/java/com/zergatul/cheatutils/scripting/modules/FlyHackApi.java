package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class FlyHackApi extends ModuleApi<FlyHackConfig> {

    @MethodDescription("Vanilla flying speed is 0.05.")
    public double getFlySpeed() {
        return getConfig().flyingSpeed;
    }

    @MethodDescription("Vanilla flying speed is 0.05.")
    public void setFlySpeed(double speed) {
        getConfig().flyingSpeed = (float)speed;
        ConfigStore.instance.requestWrite();
    }

    @Override
    protected FlyHackConfig getConfig() {
        return ConfigStore.instance.getConfig().flyHackConfig;
    }
}