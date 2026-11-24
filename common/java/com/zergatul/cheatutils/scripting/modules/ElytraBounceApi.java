package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.automation.ElytraBounce;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class ElytraBounceApi {

    public boolean isEnabled() {
        return ElytraBounce.INSTANCE.isEnabled();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void enable() {
        ElytraBounce.INSTANCE.enable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void disable() {
        ElytraBounce.INSTANCE.disable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        if (ElytraBounce.INSTANCE.isEnabled()) {
            ElytraBounce.INSTANCE.disable();
        } else {
            ElytraBounce.INSTANCE.enable();
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setEnabled(boolean enabled) {
        if (enabled) {
            ElytraBounce.INSTANCE.enable();
        } else {
            ElytraBounce.INSTANCE.disable();
        }
    }
}