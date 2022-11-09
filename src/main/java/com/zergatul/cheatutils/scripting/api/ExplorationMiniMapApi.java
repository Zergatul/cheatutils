package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.controllers.ExplorationMiniMapController;

public class ExplorationMiniMapApi {

    public void addMarker() {
        ExplorationMiniMapController.instance.addMarker();
    }
}
