package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.*;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(ModMain.MODID)
public class ModMain {

    public static final String MODID = "cheatutils";
    public static final Logger LOGGER = LogManager.getLogger(ModMain.class);

    public ModMain(IEventBus bus, ModContainer container) {
        bus.addListener(this::onCommonSetup);
        bus.addListener(this::onLoadComplete);
        bus.addListener(this::onRegisterKeybindings);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        Modules.register();
        NeoForge.EVENT_BUS.register(new NeoForgeEvents());
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
    }

    private void onRegisterKeybindings(final RegisterKeyMappingsEvent event) {
        Modules.registerKeyBindings();
        Events.RegisterKeyBindings.trigger(event::register);
    }
}