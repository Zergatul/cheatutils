package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ArmorOverlayConfig;
import com.zergatul.cheatutils.modules.visuals.ArmorOverlay;

public class ArmorOverlayApi extends SettingHandleApi<ArmorOverlayConfig> {
    public ArmorOverlayApi() {
        super(ArmorOverlay.INSTANCE.getSettings());
    }
}