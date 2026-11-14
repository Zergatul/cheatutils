package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.MixinPlugin;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.features.Features;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import com.zergatul.cheatutils.web.WebServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import org.spongepowered.asm.mixin.MixinEnvironment;

public final class ModMain implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        setupFabricEvents();

        SystemFonts.initAsync();

        // loads config
        Profiles.INSTANCE.init();
        WebServer.INSTANCE.start();
        Modules.registerKeyBindings();
        Modules.register();
        Features.register();
        // triggers initial SettingHandle<T>.observer handles registered by all modules constructors
        ConfigStore.INSTANCE.initialNotify();
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