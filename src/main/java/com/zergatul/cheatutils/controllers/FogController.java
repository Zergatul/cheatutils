package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FogConfig;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FogController {

    public static final FogController instance = new FogController();

    private FogController() {

    }

    @SubscribeEvent
    public void onRenderFog(ViewportEvent.RenderFog event) {
        FogConfig config = ConfigStore.instance.getConfig().fogConfig;
        if (config.disableFog && FogConfig.METHOD_MODIFY_FOG_DISTANCES.equals(config.method)) {
            event.setNearPlaneDistance(10000);
            event.setFarPlaneDistance(1000000);
            event.setCanceled(true);
        }
    }
}