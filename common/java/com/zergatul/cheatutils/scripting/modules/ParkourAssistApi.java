package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.automation.ParkourAssist;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class ParkourAssistApi {

    public boolean isEnabled() {
        return ParkourAssist.instance.isEnabled();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void enable() {
        ParkourAssist.instance.enable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void disable() {
        ParkourAssist.instance.disable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        if (ParkourAssist.instance.isEnabled()) {
            ParkourAssist.instance.disable();
        } else {
            ParkourAssist.instance.enable();
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setEnabled(boolean enabled) {
        if (enabled) {
            ParkourAssist.instance.enable();
        } else {
            ParkourAssist.instance.disable();
        }
    }
}