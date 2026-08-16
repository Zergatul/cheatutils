package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.ModLoaderBridge;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.wrappers.FabricEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;

public class ModMain implements ClientModInitializer {

    public static final String MODID = "cheatutils";

    public ModMain() {
        ModLoaderBridgeInstance.init(new Bridge());
    }

    @Override
    public void onInitializeClient() {
        FabricEvents.setup();

        Profiles.instance.init();
        ConfigHttpServer.instance.start();
        Modules.register();
        Events.RegisterKeyBindings.trigger(KeyBindingHelper::registerKeyBinding);
    }

    private static class Bridge implements ModLoaderBridge {

        @Override
        public String getModLoaderName() {
            return "Fabric";
        }

        @Override
        public String getModLoaderVersion() {
            return FabricLoader.getInstance()
                    .getModContainer("fabricloader")
                    .orElseThrow()
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();
        }

        @Override
        public String getModVersion() {
            return FabricLoader.getInstance()
                    .getModContainer(Constants.MOD_ID)
                    .orElseThrow()
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();
        }

        @Override
        public int getModCount() {
            return FabricLoader.getInstance().getAllMods().size();
        }
    }
}