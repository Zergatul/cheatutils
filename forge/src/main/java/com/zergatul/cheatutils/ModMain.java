package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModMain.MODID)
public class ModMain {

    public static final String MODID = "cheatutils";
    public static final Logger LOGGER = LogManager.getLogger(ModMain.class);

    public ModMain(final FMLJavaModLoadingContext context) {
        BusGroup modBusGroup = context.getModBusGroup();

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::onCommonSetup);
        RegisterKeyMappingsEvent.getBus(modBusGroup).addListener(this::onRegisterKeyMappings);
        FMLLoadCompleteEvent.getBus(modBusGroup).addListener(this::onLoadComplete);

        Modules.registerKeyBindings();
        Modules.register();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        new ForgeEvents().register();
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        Events.RegisterKeyBindings.trigger(event::register);
    }
}