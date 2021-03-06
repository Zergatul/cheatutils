package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;

public class FullBrightController {

    public static final FullBrightController instance = new FullBrightController();

    private final Minecraft mc = Minecraft.getInstance();

    public void onChanged() {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled) {
            mc.options.gamma = 15.0F;
        } else {
            mc.options.gamma = 1.0F;
        }
    }

}
