package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.chunkoverlays.NewChunksOverlay;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import com.zergatul.cheatutils.controllers.KillAuraController;
import com.zergatul.cheatutils.controllers.LightLevelController;
import com.zergatul.cheatutils.controllers.UserNameController;
import com.zergatul.cheatutils.utils.MathUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.MethodNotSupportedException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApiHandler implements HttpHandler {

    private final List<ApiBase> apis = new ArrayList<>();

    public ApiHandler() {
        apis.add(new UserApi());
        apis.add(new BlocksConfigApi());
        apis.add(new BlockInfoApi());
        apis.add(new HardSwitchApi());
        apis.add(new EntityInfoApi());
        apis.add(new EntitiesConfigApi());
        apis.add(new BlockColorApi());
        apis.add(new KillAuraInfoApi());
        apis.add(new ExplorationMiniMapMarkersApi());
        apis.add(new ScriptsApi());
        apis.add(new ScriptsAssignApi());
        apis.add(new ScriptsDocsApi());
        apis.add(new BeaconsListApi());
        apis.add(new AutoDropApi());
        apis.add(new ItemInfoApi());
        apis.add(new StatusOverlayApi());
        apis.add(new ClassNameApi());
        apis.add(new BlockEspRestartApi());

        apis.add(new SimpleConfigApi<>("full-bright", FullBrightConfig.class) {
            @Override
            protected FullBrightConfig getConfig() {
                return ConfigStore.instance.getConfig().fullBrightConfig;
            }

            @Override
            protected void setConfig(FullBrightConfig config) {
                ConfigStore.instance.getConfig().fullBrightConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("auto-fish", AutoFishConfig.class) {
            @Override
            protected AutoFishConfig getConfig() {
                return ConfigStore.instance.getConfig().autoFishConfig;
            }

            @Override
            protected void setConfig(AutoFishConfig config) {
                ConfigStore.instance.getConfig().autoFishConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("armor-overlay", ArmorOverlayConfig.class) {
            @Override
            protected ArmorOverlayConfig getConfig() {
                return ConfigStore.instance.getConfig().armorOverlayConfig;
            }

            @Override
            protected void setConfig(ArmorOverlayConfig config) {
                ConfigStore.instance.getConfig().armorOverlayConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("auto-disconnect", AutoDisconnectConfig.class) {
            @Override
            protected AutoDisconnectConfig getConfig() {
                return ConfigStore.instance.getConfig().autoDisconnectConfig;
            }

            @Override
            protected void setConfig(AutoDisconnectConfig config) {
                ConfigStore.instance.getConfig().autoDisconnectConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("boat-hack", BoatHackConfig.class) {
            @Override
            protected BoatHackConfig getConfig() {
                return ConfigStore.instance.getConfig().boatHackConfig;
            }

            @Override
            protected void setConfig(BoatHackConfig config) {
                ConfigStore.instance.getConfig().boatHackConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("elytra-hack", ElytraHackConfig.class) {
            @Override
            protected ElytraHackConfig getConfig() {
                return ConfigStore.instance.getConfig().elytraHackConfig;
            }

            @Override
            protected void setConfig(ElytraHackConfig config) {
                ConfigStore.instance.getConfig().elytraHackConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("pig-hack", PigHackConfig.class) {
            @Override
            protected PigHackConfig getConfig() {
                return ConfigStore.instance.getConfig().pigHackConfig;
            }

            @Override
            protected void setConfig(PigHackConfig config) {
                config.steeringSpeed = Math.min(Math.max(0.01f, config.steeringSpeed), 5f);
                ConfigStore.instance.getConfig().pigHackConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("kill-aura", KillAuraConfig.class) {
            @Override
            protected KillAuraConfig getConfig() {
                return ConfigStore.instance.getConfig().killAuraConfig;
            }

            @Override
            protected void setConfig(KillAuraConfig config) {
                KillAuraConfig oldConfig = ConfigStore.instance.getConfig().killAuraConfig;
                ConfigStore.instance.getConfig().killAuraConfig = config;

                if (!oldConfig.active && config.active) {
                    KillAuraController.instance.onEnabled();
                }
            }
        });

        apis.add(new SimpleConfigApi<>("light-level", LightLevelConfig.class) {
            @Override
            protected LightLevelConfig getConfig() {
                return ConfigStore.instance.getConfig().lightLevelConfig;
            }

            @Override
            protected void setConfig(LightLevelConfig config) {
                ConfigStore.instance.getConfig().lightLevelConfig = config;
                LightLevelController.instance.onChanged();
            }
        });

        apis.add(new SimpleConfigApi<>("projectile-path", ProjectilePathConfig.class) {
            @Override
            protected ProjectilePathConfig getConfig() {
                return ConfigStore.instance.getConfig().projectilePathConfig;
            }

            @Override
            protected void setConfig(ProjectilePathConfig config) {
                ConfigStore.instance.getConfig().projectilePathConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("end-city-chunks", EndCityChunksConfig.class) {
            @Override
            protected EndCityChunksConfig getConfig() {
                return ConfigStore.instance.getConfig().endCityChunksConfig;
            }

            @Override
            protected void setConfig(EndCityChunksConfig config) {
                ConfigStore.instance.getConfig().endCityChunksConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("entity-owner", EntityOwnerConfig.class) {
            @Override
            protected EntityOwnerConfig getConfig() {
                return ConfigStore.instance.getConfig().entityOwnerConfig;
            }

            @Override
            protected void setConfig(EntityOwnerConfig config) {
                ConfigStore.instance.getConfig().entityOwnerConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("shulker-tooltip", ShulkerTooltipConfig.class) {
            @Override
            protected ShulkerTooltipConfig getConfig() {
                return ConfigStore.instance.getConfig().shulkerTooltipConfig;
            }

            @Override
            protected void setConfig(ShulkerTooltipConfig config) {
                ConfigStore.instance.getConfig().shulkerTooltipConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("exploration-mini-map", ExplorationMiniMapConfig.class) {
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
        });

        apis.add(new SimpleConfigApi<>("auto-criticals", AutoCriticalsConfig.class) {
            @Override
            protected AutoCriticalsConfig getConfig() {
                return ConfigStore.instance.getConfig().autoCriticalsConfig;
            }

            @Override
            protected void setConfig(AutoCriticalsConfig config) {
                ConfigStore.instance.getConfig().autoCriticalsConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("fly-hack", FlyHackConfig.class) {
            @Override
            protected FlyHackConfig getConfig() {
                return ConfigStore.instance.getConfig().flyHackConfig;
            }

            @Override
            protected void setConfig(FlyHackConfig config) {
                ConfigStore.instance.getConfig().flyHackConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("auto-totem", AutoTotemConfig.class) {
            @Override
            protected AutoTotemConfig getConfig() {
                return ConfigStore.instance.getConfig().autoTotemConfig;
            }

            @Override
            protected void setConfig(AutoTotemConfig config) {
                ConfigStore.instance.getConfig().autoTotemConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("death-coordinates", DeathCoordinatesConfig.class) {
            @Override
            protected DeathCoordinatesConfig getConfig() {
                return ConfigStore.instance.getConfig().deathCoordinatesConfig;
            }

            @Override
            protected void setConfig(DeathCoordinatesConfig config) {
                ConfigStore.instance.getConfig().deathCoordinatesConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("elytra-tunnel", ElytraTunnelConfig.class) {
            @Override
            protected ElytraTunnelConfig getConfig() {
                return ConfigStore.instance.getConfig().elytraTunnelConfig;
            }

            @Override
            protected void setConfig(ElytraTunnelConfig config) {
                config.limit = MathUtils.clamp(config.limit, -1000, 1000);
                ConfigStore.instance.getConfig().elytraTunnelConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("free-cam", FreeCamConfig.class) {
            @Override
            protected FreeCamConfig getConfig() {
                return ConfigStore.instance.getConfig().freeCamConfig;
            }

            @Override
            protected void setConfig(FreeCamConfig config) {
                ConfigStore.instance.getConfig().freeCamConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("lock-inputs", LockInputsConfig.class) {
            @Override
            protected LockInputsConfig getConfig() {
                return ConfigStore.instance.getConfig().lockInputsConfig;
            }

            @Override
            protected void setConfig(LockInputsConfig config) {
                ConfigStore.instance.getConfig().lockInputsConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("movement-hack", MovementHackConfig.class) {
            @Override
            protected MovementHackConfig getConfig() {
                return ConfigStore.instance.getConfig().movementHackConfig;
            }

            @Override
            protected void setConfig(MovementHackConfig config) {
                ConfigStore.instance.getConfig().movementHackConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("scaffold", ScaffoldConfig.class) {
            @Override
            protected ScaffoldConfig getConfig() {
                return ConfigStore.instance.getConfig().scaffoldConfig;
            }

            @Override
            protected void setConfig(ScaffoldConfig config) {
                ConfigStore.instance.getConfig().scaffoldConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("advanced-tooltips", AdvancedTooltipsConfig.class) {
            @Override
            protected AdvancedTooltipsConfig getConfig() {
                return ConfigStore.instance.getConfig().advancedTooltipsConfig;
            }

            @Override
            protected void setConfig(AdvancedTooltipsConfig config) {
                ConfigStore.instance.getConfig().advancedTooltipsConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("fog", FogConfig.class) {
            @Override
            protected FogConfig getConfig() {
                return ConfigStore.instance.getConfig().fogConfig;
            }

            @Override
            protected void setConfig(FogConfig config) {
                ConfigStore.instance.getConfig().fogConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("instant-disconnect", InstantDisconnectConfig.class) {
            @Override
            protected InstantDisconnectConfig getConfig() {
                return ConfigStore.instance.getConfig().instantDisconnectConfig;
            }

            @Override
            protected void setConfig(InstantDisconnectConfig config) {
                ConfigStore.instance.getConfig().instantDisconnectConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("beacons", BeaconConfig.class) {
            @Override
            protected BeaconConfig getConfig() {
                BeaconConfig config = new BeaconConfig();
                config.enabled = ConfigStore.instance.getConfig().beaconConfig.enabled;
                return config;
            }

            @Override
            protected void setConfig(BeaconConfig config) {
                ConfigStore.instance.getConfig().beaconConfig.enabled = config.enabled;
            }
        });

        apis.add(new SimpleConfigApi<>("user-name", UserNameConfig.class) {
            @Override
            protected UserNameConfig getConfig() {
                return ConfigStore.instance.getConfig().userNameConfig;
            }

            @Override
            protected void setConfig(UserNameConfig config) {
                ConfigStore.instance.getConfig().userNameConfig = config;
                UserNameController.instance.onConfigUpdated();
            }
        });

        apis.add(new SimpleConfigApi<>("new-chunks", NewChunksConfig.class) {
            @Override
            protected NewChunksConfig getConfig() {
                return ConfigStore.instance.getConfig().newChunksConfig;
            }

            @Override
            protected void setConfig(NewChunksConfig config) {
                NewChunksConfig oldConfig = ConfigStore.instance.getConfig().newChunksConfig;
                ConfigStore.instance.getConfig().newChunksConfig = config;

                if (oldConfig.enabled != config.enabled) {
                    ChunkOverlayController.instance.ofType(NewChunksOverlay.class).onEnabledChanged();
                }
            }
        });

        apis.add(new SimpleConfigApi<>("chunks", ChunksConfig.class) {
            @Override
            protected ChunksConfig getConfig() {
                return ConfigStore.instance.getConfig().chunksConfig;
            }

            @Override
            protected void setConfig(ChunksConfig config) {
                ConfigStore.instance.getConfig().chunksConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("container-buttons", ContainerButtonsConfig.class) {
            @Override
            protected ContainerButtonsConfig getConfig() {
                return ConfigStore.instance.getConfig().containerButtonsConfig;
            }

            @Override
            protected void setConfig(ContainerButtonsConfig config) {
                ConfigStore.instance.getConfig().containerButtonsConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("status-overlay", StatusOverlayConfig.class) {
            @Override
            protected StatusOverlayConfig getConfig() {
                return ConfigStore.instance.getConfig().statusOverlayConfig;
            }

            @Override
            protected void setConfig(StatusOverlayConfig config) {
                ConfigStore.instance.getConfig().statusOverlayConfig.enabled = config.enabled;
            }
        });

        apis.add(new SimpleConfigApi<>("status-effects", StatusEffectsConfig.class) {
            @Override
            protected StatusEffectsConfig getConfig() {
                return ConfigStore.instance.getConfig().statusEffectsConfig;
            }

            @Override
            protected void setConfig(StatusEffectsConfig config) {
                ConfigStore.instance.getConfig().statusEffectsConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("auto-eat", AutoEatConfig.class) {
            @Override
            protected AutoEatConfig getConfig() {
                return ConfigStore.instance.getConfig().autoEatConfig;
            }

            @Override
            protected void setConfig(AutoEatConfig config) {
                ConfigStore.instance.getConfig().autoEatConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("no-fall", NoFallConfig.class) {
            @Override
            protected NoFallConfig getConfig() {
                return ConfigStore.instance.getConfig().noFallConfig;
            }

            @Override
            protected void setConfig(NoFallConfig config) {
                ConfigStore.instance.getConfig().noFallConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("particles", ParticlesConfig.class) {
            @Override
            protected ParticlesConfig getConfig() {
                return ConfigStore.instance.getConfig().particlesConfig;
            }

            @Override
            protected void setConfig(ParticlesConfig config) {
                ConfigStore.instance.getConfig().particlesConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("anti-respawn-reset", AntiRespawnResetConfig.class) {
            @Override
            protected AntiRespawnResetConfig getConfig() {
                return ConfigStore.instance.getConfig().antiRespawnResetConfig;
            }

            @Override
            protected void setConfig(AntiRespawnResetConfig config) {
                ConfigStore.instance.getConfig().antiRespawnResetConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("fast-break", FastBreakConfig.class) {
            @Override
            protected FastBreakConfig getConfig() {
                return ConfigStore.instance.getConfig().fastBreakConfig;
            }

            @Override
            protected void setConfig(FastBreakConfig config) {
                ConfigStore.instance.getConfig().fastBreakConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("container-summary", ContainerSummaryConfig.class) {
            @Override
            protected ContainerSummaryConfig getConfig() {
                return ConfigStore.instance.getConfig().containerSummaryConfig;
            }

            @Override
            protected void setConfig(ContainerSummaryConfig config) {
                ConfigStore.instance.getConfig().containerSummaryConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("auto-craft", AutoCraftConfig.class) {
            @Override
            protected AutoCraftConfig getConfig() {
                return ConfigStore.instance.getConfig().autoCraftConfig;
            }

            @Override
            protected void setConfig(AutoCraftConfig config) {
                ConfigStore.instance.getConfig().autoCraftConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<>("reach", ReachConfig.class) {
            @Override
            protected ReachConfig getConfig() {
                return ConfigStore.instance.getConfig().reachConfig;
            }

            @Override
            protected void setConfig(ReachConfig config) {
                ConfigStore.instance.getConfig().reachConfig = config;
            }
        });
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String[] parts = exchange.getRequestURI().getPath().split("/");

        Optional<ApiBase> api;
        synchronized (apis) {
            api = apis.stream().filter(a -> a.getRoute().equals(parts[2])).findFirst();
        }

        if (!api.isPresent()) {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
            return;
        }

        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    processGet(parts, api.get(), exchange);
                    break;
                case "POST":
                    processPost(api.get(), exchange);
                    break;
                case "PUT":
                    processPut(parts, api.get(), exchange);
                    break;
                case "DELETE":
                    processDelete(parts, api.get(), exchange);
                    break;
            }
        }
        catch (MethodNotSupportedException e) {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
        }
        catch (NotFoundHttpException e) {
            byte[] data = e.getMessage().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, 0);
            OutputStream stream = exchange.getResponseBody();
            stream.write(data);
            stream.close();
            exchange.close();
        }
        catch (HttpException e) {
            sendMessage(exchange, 503, e.getMessage());
        }
        catch (Exception e) {
            sendMessage(exchange, 500, e.getMessage());
        }
    }

    private void processGet(String[] parts, ApiBase api, HttpExchange exchange) throws HttpException, IOException {
        String response;
        if (parts.length == 3) {
            response = api.get();
        } else {
            response = api.get(parts[3]);
        }
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(200, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();
    }

    private void processPost(ApiBase api, HttpExchange exchange) throws HttpException, IOException {

        String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
        String response = api.post(body);

        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(200, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();
    }

    private void processPut(String[] parts, ApiBase api, HttpExchange exchange) throws HttpException, IOException {

        if (parts.length < 4) {
            throw new MethodNotSupportedException("PUT requires id");
        }

        String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
        api.put(parts[3], body);

        byte[] data = "{}".getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(200, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();

    }

    private void processDelete(String[] parts, ApiBase api, HttpExchange exchange) throws HttpException, IOException {

        if (parts.length < 4) {
            throw new MethodNotSupportedException("DELETE requires id");
        }

        String response = api.delete(parts[3]);
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(200, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();

    }

    private void sendMessage(HttpExchange exchange, int code, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(bytes);
        stream.close();
        exchange.close();
    }
}
