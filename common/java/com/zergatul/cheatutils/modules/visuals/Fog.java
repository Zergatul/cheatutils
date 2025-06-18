package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.FogRendererAccessor;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;

public class Fog implements Module {

    public static final Fog instance = new Fog();

    private Fog() {
        Events.ClientTickStart.add(this::onClientTickStart);
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
}