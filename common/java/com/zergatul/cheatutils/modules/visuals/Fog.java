package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.FogConfig;
import com.zergatul.cheatutils.mixins.common.accessors.FogRendererAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;

public class Fog implements Module {

    public static final Fog INSTANCE = new Fog();

    private final SettingHandle2<FogConfig> settings = Settings.get(FogConfig.class);

    private Fog() {
        Events.InGameTickStart.add(this::onClientTickStart);

        WebApiRegistry.INSTANCE.register("fog", settings, FogConfig.class);
    }

    public SettingHandle2<FogConfig> getSettings() {
        return settings;
    }

    public void onClientTickStart() {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        if (settings.get().enabled()) {
            if (FogRendererAccessor.isFogEnabled_CU()) {
                FogRenderer.toggleFog();
            }
        } else {
            if (!FogRendererAccessor.isFogEnabled_CU()) {
                FogRenderer.toggleFog();
            }
        }
    }
}