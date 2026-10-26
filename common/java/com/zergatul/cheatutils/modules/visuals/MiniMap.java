package com.zergatul.cheatutils.modules.visuals;

import com.google.common.reflect.TypeToken;
import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.ExplorationMiniMapConfig;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MiniMap implements Module {

    public static final MiniMap INSTANCE = new MiniMap();

    private final SettingHandle2<ExplorationMiniMapConfig> settings = Settings.get(ExplorationMiniMapConfig.class);

    private MiniMap() {
        settings.onChanged(this::onConfigChanged);

        WebApiRegistry.INSTANCE.register("exploration-mini-map", settings, ExplorationMiniMapConfig.class);
        WebApiRegistry.INSTANCE.register(new MarkersWebApi());
    }

    public SettingHandle2<ExplorationMiniMapConfig> getSettings() {
        return settings;
    }

    private void onConfigChanged(ExplorationMiniMapConfig prev, ExplorationMiniMapConfig curr) {
        if (prev.enabled() != curr.enabled()) {
            ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).onEnabledChanged();
        }
    }

    private static final class MarkersWebApi extends WebApiBase {

        @Override
        public String getRoute() {
            return "exploration-mini-map-markers";
        }

        @Override
        public String post(String body) {
            ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).addMarker();
            return "true";
        }

        @Override
        public String put(String id, String body) {
            if (Objects.equals(id, "import")) {
                Type listType = new TypeToken<ArrayList<Point>>(){}.getType();
                List<Point> points = gson.fromJson(body, listType);
                points.forEach(p -> {
                    if (Double.isNaN(p.x)) {
                        return;
                    }
                    if (Double.isNaN(p.z)) {
                        return;
                    }
                    ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).addMarker(p.x, p.z);
                });
                return "{ \"ok\": true }";
            } else {
                return "{}";
            }
        }

        @Override
        public String delete(String id) {
            ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).clearMarkers();
            return "true";
        }

        public static class Point {
            public double x = Double.NaN;
            public double z = Double.NaN;
        }
    }
}