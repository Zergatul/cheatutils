package com.zergatul.cheatutils.forge;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraftforge.common.MinecraftForge;
import com.zergatul.cheatutils.configs.ConfigWriterQueue;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, useMetadata = true,
        clientSideOnly = true, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.12.2]")
public final class ModMain {
    private static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        LOGGER.info("CheatUtils {} initializing on Forge 1.12.2.", event.getModMetadata().version);
        Events.Close.add(ConfigHttpServer.instance::close);
        Events.Close.add(ConfigWriterQueue.instance::close);
        Events.ConfigLoaded.add(ConfigHttpServer.instance::onConfigUpdated);
        Events.ConfigLoaded.add(FreeCam.instance::disable);
        Events.Close.add(FreeCam.instance::disable, -1);
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
        Profiles.instance.init(event.getModConfigurationDirectory());
        ConfigHttpServer.instance.start(Minecraft.getMinecraft().gameDir);
    }
}
