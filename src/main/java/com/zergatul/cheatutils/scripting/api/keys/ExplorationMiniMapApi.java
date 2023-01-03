package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ExplorationMiniMapConfig;
import com.zergatul.cheatutils.controllers.ExplorationMiniMapController;

public class ExplorationMiniMapApi {

    public void addMarker() {
        ExplorationMiniMapController.instance.addMarker();
    }

    public void toggle() {
        ExplorationMiniMapConfig config = ConfigStore.instance.getConfig().explorationMiniMapConfig;
        config.enabled = !config.enabled;
        ExplorationMiniMapController.instance.onChanged();
        ConfigStore.instance.requestWrite();
    }
}