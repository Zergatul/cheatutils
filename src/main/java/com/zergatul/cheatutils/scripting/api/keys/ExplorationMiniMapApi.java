package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.controllers.ExplorationMiniMapController;

public class ExplorationMiniMapApi {

    public void addMarker() {
        ExplorationMiniMapController.instance.addMarker();
    }
}
