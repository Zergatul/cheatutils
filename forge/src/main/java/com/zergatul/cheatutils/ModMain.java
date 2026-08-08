package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.ModLoaderBridge;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.forge.ArmorGuiOverlay;
import com.zergatul.cheatutils.forge.BetterStatusEffectsGuiOverlay;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.automation.*;
import com.zergatul.cheatutils.modules.hacks.*;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.wrappers.ForgeEvents;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.versions.forge.ForgeVersion;

@Mod(Constants.MOD_ID)
public class ModMain {

    public ModMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterKeyMappings);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGuiOverlay);

        ModLoaderBridgeInstance.init(new Bridge());
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
        Modules.register();
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        Events.RegisterKeyBindings.trigger(event::register);
    }

    private void onRegisterGuiOverlay(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("armor", new ArmorGuiOverlay());
        event.registerAboveAll("betterstatus", new BetterStatusEffectsGuiOverlay());
    }

    private static class Bridge implements ModLoaderBridge {

        @Override
        public String getModLoaderName() {
            return "Forge";
        }

        @Override
        public String getModLoaderVersion() {
            return ForgeVersion.getVersion();
        }

        @Override
        public String getModVersion() {
            return ModList.get().getModFileById(Constants.MOD_ID).getMods().get(0).getVersion().toString();
        }

        @Override
        public int getModCount() {
            return ModList.get().getMods().size();
        }
    }
}