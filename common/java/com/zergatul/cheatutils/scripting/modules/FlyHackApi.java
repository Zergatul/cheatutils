package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.FlyHackConfig;
import com.zergatul.cheatutils.modules.hacks.FlyHack;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class FlyHackApi extends SettingHandleApi<FlyHackConfig> {

    public FlyHackApi() {
        super(FlyHack.instance.getSettings());
    }

    @MethodDescription("Vanilla flying speed is 0.05.")
    public double getFlySpeed() {
        return getConfig().flyingSpeed;
    }

    @MethodDescription("Vanilla flying speed is 0.05.")
    public void setFlySpeed(double speed) {
        update(c -> c.flyingSpeed = (float) speed);
    }
}