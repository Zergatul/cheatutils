package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.controllers.FakeLagController;

public class FakeLagApi {

    public boolean isEnabled() {
        return FakeLagController.instance.isEnabled();
    }

    public void toggle() {
        FakeLagController.instance.toggle();
    }
}
