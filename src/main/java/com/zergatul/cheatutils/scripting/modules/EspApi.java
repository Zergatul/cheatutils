package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class EspApi {

    @MethodDescription("Enables/disables rendering of all ESP modules")
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {}
}