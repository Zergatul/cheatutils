package com.zergatul.cheatutils;

import com.mojang.logging.LogUtils;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ModMain.MODID)
public class ModMain {
    public static final String MODID = "cheatutils";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModMain() {
        ConfigHttpServer.instance.start();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ModLoader::setup);
    }
}
