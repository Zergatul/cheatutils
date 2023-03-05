package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

public class ExplorationMiniMapApi {

    @ApiVisibility(ApiType.UPDATE)
    public void addMarker() {
        ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).addMarker();
    }
}