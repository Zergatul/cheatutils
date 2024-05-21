package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.SetupFogEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FogConfig;
import com.zergatul.cheatutils.modules.Module;

public class Fog implements Module {

    public static final Fog instance = new Fog();

    private Fog() {
        Events.SetupFog.add(this::onSetupFog);
    }

    public void onSetupFog(SetupFogEvent event) {
        FogConfig config = ConfigStore.instance.getConfig().fogConfig;
        if (config.enabled && FogConfig.METHOD_MODIFY_FOG_DISTANCES.equals(config.method)) {
            event.setFogStart(10000);
            event.setFogEnd(1000000);
        }
    }
}