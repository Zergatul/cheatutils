package com.zergatul.cheatutils.modules.visuals;

import com.google.common.reflect.TypeToken;
import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ExplorationMiniMapConfig;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MiniMap implements Module {

    public static final MiniMap INSTANCE = new MiniMap();

    private MiniMap() {
        WebApiRegistry.INSTANCE.register(new ConfigWebApi());
        WebApiRegistry.INSTANCE.register(new MarkersWebApi());
    }

    private static final class ConfigWebApi extends SimpleModuleConfigWebApi<ExplorationMiniMapConfig> {

        public ConfigWebApi() {
            super("exploration-mini-map", ExplorationMiniMapConfig.class);
        }

        @Override
        protected ExplorationMiniMapConfig getConfig() {
            return ConfigStore.instance.getConfig().explorationMiniMapConfig;
        }

        @Override
        protected void setConfig(ExplorationMiniMapConfig config) {
            ExplorationMiniMapConfig oldConfig = ConfigStore.instance.getConfig().explorationMiniMapConfig;
            ConfigStore.instance.getConfig().explorationMiniMapConfig = config;

            if (oldConfig.enabled != config.enabled) {
                ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class).onEnabledChanged();
            }
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