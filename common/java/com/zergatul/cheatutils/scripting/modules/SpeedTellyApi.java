package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.hacks.SpeedTelly;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class SpeedTellyApi {

    @ApiVisibility(ApiType.UPDATE)
    public void enable() {
        SpeedTelly.instance.enable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void disable() {
        SpeedTelly.instance.disable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setEnabled(boolean enabled) {
        if (enabled) {
            SpeedTelly.instance.enable();
        } else {
            SpeedTelly.instance.disable();
        }
    }
}