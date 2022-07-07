package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.controllers.ExplorationMiniMapController;
import org.apache.http.HttpException;

public class ExplorationMiniMapMarkersApi extends ApiBase {

    @Override
    public String getRoute() {
        return "exploration-mini-map-markers";
    }

    @Override
    public String post(String body) throws HttpException {
        ExplorationMiniMapController.instance.addMarker();
        return "true";
    }

    @Override
    public String delete(String id) throws HttpException {
        ExplorationMiniMapController.instance.clearMarkers();
        return "true";
    }
}