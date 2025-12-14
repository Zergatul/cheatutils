package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.hacks.AirPlace;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

public class AirPlaceApi {

    @MethodDescription("""
            Checks if Air Place is active
            """)
    public boolean isEnabled() {
        return AirPlace.instance.isActive();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void enable() {
        AirPlace.instance.enable();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void disable() {
        AirPlace.instance.disable();
    }

    @MethodDescription("""
            Toggles Air Place status
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        AirPlace.instance.toggle();
    }
}