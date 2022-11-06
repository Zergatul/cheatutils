package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import org.apache.http.HttpException;

public class ExplorationMiniMapMarkersApi extends ApiBase {

    @Override
    public String getRoute() {
        return "exploration-mini-map-markers";
    }

    @Override
    public String post(String body) throws HttpException {
        ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).addMarker();
        return "true";
    }

    @Override
    public String delete(String id) throws HttpException {
        ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).clearMarkers();
        return "true";
    }
}