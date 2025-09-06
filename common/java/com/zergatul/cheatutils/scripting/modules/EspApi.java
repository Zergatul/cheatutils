package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.esp.EspGlobal;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

public class EspApi {

    @MethodDescription("""
            Checks if ESPs rendering is enabled
            """)
    public boolean isEnabled() {
        return EspGlobal.enabled;
    }

    @MethodDescription("""
            Enables/disables rendering of all ESP modules
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        EspGlobal.enabled = !EspGlobal.enabled;
    }
}