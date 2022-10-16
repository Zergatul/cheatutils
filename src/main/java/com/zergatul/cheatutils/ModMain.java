package com.zergatul.cheatutils;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.wrappers.IKeyBindingRegistry;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModMain.MODID)
public class ModMain {

    public static final String MODID = "cheatutils";
    private static final Logger logger = LogManager.getLogger(ModMain.class);

    public ModMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterKeyMappings);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGuiOverlay);

        register(KeyBindingsController.instance);
        register(ChunkController.instance);
        register(RenderController.instance);
        register(NetworkPacketsController.instance);
        register(TeleportController.instance);
        register(SpeedCounterController.instance);
    }

    private void register(Object instance) {
        logger.info("Registered: {}", instance.getClass().getName());
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        ConfigHttpServer.instance.start();

        ConfigStore.instance.read();
        MinecraftForge.EVENT_BUS.register(ModApiWrapper.forgeEvents);

        MinecraftForge.EVENT_BUS.register(FreeCamController.instance);
        MinecraftForge.EVENT_BUS.register(DebugScreenController.instance);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        ModApiWrapper.triggerOnRegisterKeyBindings(ClientRegistry::registerKeyBinding);
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
    }
}