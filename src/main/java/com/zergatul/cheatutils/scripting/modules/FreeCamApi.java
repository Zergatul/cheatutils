package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.esp.FreeCam;

public class FreeCamApi {
    public boolean isEnabled() {
        return FreeCam.instance.isActive();
    }

    public void toggle() {
        FreeCam.instance.toggle();
    }

    public void enable() {
        FreeCam.instance.enable();
    }

    public void disable() {
        FreeCam.instance.disable();
    }
}
