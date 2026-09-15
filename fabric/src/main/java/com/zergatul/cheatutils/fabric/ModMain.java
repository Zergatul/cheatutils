package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.MixinPlugin;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class ModMain implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        setupFabricEvents();

        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
        Modules.registerKeyBindings();
        Modules.register();
        Events.RegisterKeyBindings.trigger(KeyMappingHelper::registerKeyMapping);
        DebugScreenExtensions.register();

        if (MixinPlugin.isStrictMixinsEnabled()) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }

    private void setupFabricEvents() {
        ClientChunkEvents.CHUNK_LOAD.register((_, chunk) -> Events.RawChunkLoaded.trigger(chunk));
        ClientChunkEvents.CHUNK_UNLOAD.register((_, chunk) -> Events.RawChunkUnloaded.trigger(chunk));
    }
}