package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FreeCamConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;

public class FreeCamApi {

    public boolean isEnabled() {
        return FreeCamController.instance.isActive();
    }

    public void toggle() {
        FreeCamController.instance.toggle();
    }

    public void toggleRenderHands() {
        var config = getConfig();
        config.renderHands = !config.renderHands;
        ConfigStore.instance.requestWrite();
    }

    public void setRenderHands(boolean value) {
        var config = getConfig();
        config.renderHands = value;
        ConfigStore.instance.requestWrite();
    }

    private FreeCamConfig getConfig() {
        return ConfigStore.instance.getConfig().freeCamConfig;
    }
}