package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.hacks.FakeLag;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;

public class FakeLagApi {

    @ApiVisibility(ApiType.UPDATE)
    public void enable() {
        if (!isEnabled()) {
            toggle();
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void disable() {
        if (isEnabled()) {
            toggle();
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setEnabled(boolean value) {
        if (value) {
            enable();
        } else {
            disable();
        }
    }

    public boolean isEnabled() {
        return FakeLag.instance.isEnabled();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        FakeLag.instance.toggle();
    }
}