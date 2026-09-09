package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class FreeCamApi {

    @MethodDescription("Checks if Free Cam is active")
    public boolean isEnabled() {
        return FreeCam.INSTANCE.isActive();
    }

    @MethodDescription("Toggles Free Cam status")
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        FreeCam.INSTANCE.toggle();
    }
}