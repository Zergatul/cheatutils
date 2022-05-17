package com.zergatul.cheatutils;

import com.mojang.logging.LogUtils;
import com.zergatul.cheatutils.controllers.ArmorOverlayController;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ModMain.MODID)
public class ModMain {
    public static final String MODID = "cheatutils";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ConfigHttpServer.instance.start();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ModLoader::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ArmorOverlayController.instance.register();
    }
}
