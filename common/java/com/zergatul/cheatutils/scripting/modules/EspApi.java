package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.Config;
import com.zergatul.cheatutils.configs.ConfigStore;

public class EspApi {

    public boolean isEnabled() {
        return ConfigStore.instance.getConfig().esp;
    }

    public void toggle() {
        ConfigStore store = ConfigStore.instance;
        Config config = store.getConfig();
        config.esp = !config.esp;
        store.requestWrite();
    }
}