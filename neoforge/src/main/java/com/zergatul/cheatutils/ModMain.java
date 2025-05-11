package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.PreRenderGuiExecutor;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.automation.*;
import com.zergatul.cheatutils.modules.esp.*;
import com.zergatul.cheatutils.modules.hacks.*;
import com.zergatul.cheatutils.modules.scripting.*;
import com.zergatul.cheatutils.modules.utilities.*;
import com.zergatul.cheatutils.modules.visuals.*;
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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Mod(ModMain.MODID)
public class ModMain {

    public static final String MODID = "cheatutils";
    public static final Logger LOGGER = LogManager.getLogger(ModMain.class);

    private final List<Module> modules = new ArrayList<>();

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
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
    }

    private void register(Module module) {
        modules.add(module);
    }

    private void onRegisterKeybindings(final RegisterKeyMappingsEvent event) {
        Modules.registerKeyBindings();
        Events.RegisterKeyBindings.trigger(event::register);
    }
}