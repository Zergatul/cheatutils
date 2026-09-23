package com.zergatul.cheatutils.neoforge;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.*;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.List;

import static com.zergatul.cheatutils.common.Events.*;

@Mod(Constants.MOD_ID)
public class ModMain {

    public ModMain(IEventBus bus, ModContainer container) {
        bus.addListener(this::onCommonSetup);
        bus.addListener(this::onLoadComplete);
        bus.addListener(this::onRegisterKeybindings);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        Modules.register();
        NeoForge.EVENT_BUS.register(new NeoForgeEvents());
        DebugScreenExtensions.register();
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

    private static class NeoForgeEvents {

        @SubscribeEvent
        public void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
            List<Component> list = new ArrayList<>();
            GatherTooltipComponents.trigger(new GatherTooltipComponentsEvent(event.getItemStack(), list));
            list.forEach(c -> event.getTooltipElements().add(Either.left(c)));
        }

        @SubscribeEvent
        public void onChunkLoad(ChunkEvent.Load event) {
            if (event.getLevel().isClientSide()) {
                RawChunkLoaded.trigger(event.getChunk());
            }
        }

        @SubscribeEvent
        public void onChunkUnload(ChunkEvent.Unload event) {
            if (event.getLevel().isClientSide()) {
                RawChunkUnloaded.trigger(event.getChunk());
            }
        }
    }
}