package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FogConfig;
import com.zergatul.cheatutils.mixins.common.accessors.FogRendererAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;

public class Fog implements Module {

    public static final Fog instance = new Fog();

    private Fog() {
        Events.InGameTickStart.add(this::onClientTickStart);

        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    public void onClientTickStart() {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        if (ConfigStore.instance.getConfig().fogConfig.enabled) {
            if (FogRendererAccessor.isFogEnabled_CU()) {
                FogRenderer.toggleFog();
            }
        } else {
            if (!FogRendererAccessor.isFogEnabled_CU()) {
                FogRenderer.toggleFog();
            }
        }
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<FogConfig> {

        public WebApi() {
            super("fog", FogConfig.class);
        }

        @Override
        protected FogConfig getConfig() {
            return ConfigStore.instance.getConfig().fogConfig;
        }

        @Override
        protected void setConfig(FogConfig config) {
            ConfigStore.instance.getConfig().fogConfig = config;
        }
    }
}