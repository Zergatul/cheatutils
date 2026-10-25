package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.ShulkerTooltipConfig;
import com.zergatul.cheatutils.modules.visuals.ShulkerTooltip;

public class ShulkerTooltipApi extends SettingHandle2Api<ShulkerTooltipConfig> {
    public ShulkerTooltipApi() {
        super(ShulkerTooltip.INSTANCE.getSettings());
    }
}