package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.modules.esp.EspGlobal;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

public class EspApi {

    @MethodDescription("Checks if ESP rendering is enabled.")
    public boolean isEnabled() {
        return EspGlobal.enabled;
    }

    @MethodDescription("Enables or disables rendering of all ESP modules.")
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        EspGlobal.enabled = !EspGlobal.enabled;
    }
}