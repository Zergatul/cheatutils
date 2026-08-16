package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.HelpText;

public class EspApi {

    @HelpText("Checks if ESP rendering is enabled.")
    public boolean isEnabled() {
        return ConfigStore.instance.getConfig().esp;
    }

    @HelpText("Enables or disables rendering of all ESP modules.")
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        ConfigStore.instance.getConfig().esp = !ConfigStore.instance.getConfig().esp;
        ConfigStore.instance.requestWrite();
    }
}