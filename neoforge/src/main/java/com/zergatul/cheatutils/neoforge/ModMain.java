package com.zergatul.cheatutils.neoforge;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.ModLifecycle;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.List;

import static com.zergatul.cheatutils.common.Events.*;

/** Constructed before Minecraft exists: only adds listeners. */
@Mod(Constants.MOD_ID)
public final class ModMain {

    public ModMain(IEventBus modBus) {
        modBus.addListener(ModMain::onRegisterKeyMappings);
        modBus.addListener(ModMain::onRegisterDebugEntries);
        NeoForge.EVENT_BUS.register(new NeoForgeEvents());
    }

    private static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        ModLifecycle.registerKeyMappings(event::register);
    }

    private static void onRegisterDebugEntries(final RegisterDebugEntriesEvent event) {
        DebugScreenExtensions.register(event::register);
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