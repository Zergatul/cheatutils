package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.controllers.FakeLagController;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

public class FakeLagApi {

    public boolean isEnabled() {
        return FakeLagController.instance.isEnabled();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        FakeLagController.instance.toggle();
    }
}