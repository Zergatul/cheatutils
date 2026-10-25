package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.automation.ParkourAssist;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class ParkourAssistApi {

    public boolean isEnabled() {
        return ParkourAssist.INSTANCE.isEnabled();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void enable() {
        ParkourAssist.INSTANCE.enable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void disable() {
        ParkourAssist.INSTANCE.disable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        if (ParkourAssist.INSTANCE.isEnabled()) {
            ParkourAssist.INSTANCE.disable();
        } else {
            ParkourAssist.INSTANCE.enable();
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setEnabled(boolean enabled) {
        if (enabled) {
            ParkourAssist.INSTANCE.enable();
        } else {
            ParkourAssist.INSTANCE.disable();
        }
    }
}