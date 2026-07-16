package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

public class ExplorationMiniMapApi {

    @MethodDescription("Adds a marker at the player's current position in the current dimension.")
    @ApiVisibility(ApiType.UPDATE)
    public void addMarker() {
        ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).addMarker();
    }
}