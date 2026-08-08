package com.zergatul.cheatutils.webui;

import com.google.common.reflect.TypeToken;
import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import org.apache.http.HttpException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExplorationMiniMapMarkersApi extends ApiBase {

    @Override
    public String getRoute() {
        return "exploration-mini-map-markers";
    }

    @Override
    public String post(String body) throws HttpException {
        ClientThreadDispatcher.run(() ->
                ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).addMarker());
        return "true";
    }

    @Override
    public String put(String id, String body) throws HttpException {
        if (!Objects.equals(id, "import")) {
            throw new ApiException("Unsupported marker operation: " + id, HttpResponseCodes.BAD_REQUEST);
        }

        Type listType = new TypeToken<ArrayList<Point>>(){}.getType();
        List<Point> points = WebHelper.parseJson(gson, body, listType);
        ClientThreadDispatcher.run(() -> {
            ExplorationMiniMapChunkOverlay overlay = ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class);
            points.forEach(p -> {
                if (p != null && Double.isFinite(p.x) && Double.isFinite(p.z)) {
                    overlay.addMarker(p.x, p.z);
                }
            });
        });
        return "{ \"ok\": true }";
    }

    @Override
    public String delete(String id) throws HttpException {
        if (!Objects.equals(id, "all")) {
            throw new ApiException("Unsupported marker operation: " + id, HttpResponseCodes.BAD_REQUEST);
        }
        ClientThreadDispatcher.run(() ->
                ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).clearMarkers());
        return "true";
    }

    public static class Point {
        public double x = Double.NaN;
        public double z = Double.NaN;
    }
}