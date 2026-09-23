package com.zergatul.cheatutils.forge;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;

@Mod(Constants.MOD_ID)
public final class ModMain {

    public ModMain(final FMLJavaModLoadingContext context) {
        BusGroup modBusGroup = context.getModBusGroup();

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::onCommonSetup);
        RegisterKeyMappingsEvent.BUS.addListener(this::onRegisterKeyMappings);
        FMLLoadCompleteEvent.getBus(modBusGroup).addListener(this::onLoadComplete);

        Modules.registerKeyBindings();
        Modules.register();
        DebugScreenExtensions.register();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        RenderTooltipEvent.GatherComponents.BUS.addListener(ForgeEvents::onPreRenderTooltip);
        ChunkEvent.Load.BUS.addListener(ForgeEvents::onChunkLoad);
        ChunkEvent.Unload.BUS.addListener(ForgeEvents::onChunkUnload);
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        Events.RegisterKeyBindings.trigger(event::register);
    }

    private static class ForgeEvents {

        private static void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
            List<Component> list = new ArrayList<>();
            Events.GatherTooltipComponents.trigger(new GatherTooltipComponentsEvent(event.getItemStack(), list));
            list.forEach(c -> event.getTooltipElements().add(Either.left(c)));
        }

        private static void onChunkLoad(ChunkEvent.Load event) {
            if (event.getLevel().isClientSide()) {
                Events.RawChunkLoaded.trigger((LevelChunk) event.getChunk());
            }
        }

        private static void onChunkUnload(ChunkEvent.Unload event) {
            if (event.getLevel().isClientSide()) {
                Events.RawChunkUnloaded.trigger((LevelChunk) event.getChunk());
            }
        }
    }
}