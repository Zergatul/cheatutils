package com.zergatul.cheatutils.forge;

import com.zergatul.cheatutils.Constants;
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
    }
}
