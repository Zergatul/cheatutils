package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ShulkerTooltipConfig;
import com.zergatul.cheatutils.modules.visuals.ShulkerTooltip;

public class ShulkerTooltipApi extends SettingHandleApi<ShulkerTooltipConfig> {

    public ShulkerTooltipApi() {
        super(ShulkerTooltip.instance.getSettings());
    }
}