package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModMain implements ClientModInitializer {

    public static final String MODID = "cheatutils";
    public static final Logger LOGGER = LogManager.getLogger(ModMain.class);

    @Override
    public void onInitializeClient() {
        FabricEvents.setup();

        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
        Modules.registerKeyBindings();
        Modules.register();
        Events.RegisterKeyBindings.trigger(KeyBindingHelper::registerKeyBinding);
        DebugScreenExtensions.register();
    }
}