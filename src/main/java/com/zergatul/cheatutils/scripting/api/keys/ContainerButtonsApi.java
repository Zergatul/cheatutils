package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;

public class ContainerButtonsApi {
    public void toggleDropAll() {
        ContainerButtonsConfig config = ConfigStore.instance.getConfig().containerButtonsConfig;
        config.showDropAll = !config.showDropAll;
        ConfigStore.instance.requestWrite();
    }
}