package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;

public class FullBrightController {

    public static final FullBrightController instance = new FullBrightController();

    public boolean insideUpdateLightTexture;
}
