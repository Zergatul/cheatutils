package com.zergatul.cheatutils.forge;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import com.zergatul.cheatutils.modules.Modules;
import net.minecraftforge.common.MinecraftForge;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = Constants.MOD_ID,
        name = Constants.MOD_NAME,
        useMetadata = true,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "[1.12.2]")
public final class ModMain {

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        Modules.register();
        Events.RegisterKeyBindings.trigger(ClientRegistry::registerKeyBinding);
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
        Profiles.instance.init(event.getModConfigurationDirectory());
        ConfigHttpServer.instance.start();
    }
}