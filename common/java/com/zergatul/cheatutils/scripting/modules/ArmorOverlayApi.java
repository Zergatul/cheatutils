package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.ArmorOverlayConfig;
import com.zergatul.cheatutils.modules.visuals.ArmorOverlay;

public class ArmorOverlayApi extends SettingHandle2Api<ArmorOverlayConfig> {
    public ArmorOverlayApi() {
        super(ArmorOverlay.INSTANCE.getSettings());
    }
}