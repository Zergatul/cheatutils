package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FullBrightConfig;
import com.zergatul.cheatutils.controllers.FullBrightController;

public class FullBrightApi {
    public void toggle() {
        FullBrightConfig config = ConfigStore.instance.getConfig().fullBrightConfig;
        config.enabled = !config.enabled;
        FullBrightController.instance.onChanged();
        ConfigStore.instance.requestWrite();
    }
}