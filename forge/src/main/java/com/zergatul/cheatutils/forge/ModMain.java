package com.zergatul.cheatutils.forge;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.ModLifecycle;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/** Constructed on a mod loading thread: only adds listeners. */
@Mod(Constants.MOD_ID)
public final class ModMain {

    public ModMain() {
        RegisterKeyMappingsEvent.BUS.addListener(ModMain::onRegisterKeyMappings);
        RenderTooltipEvent.GatherComponents.BUS.addListener(ForgeEvents::onPreRenderTooltip);
        ChunkEvent.Load.BUS.addListener(ForgeEvents::onChunkLoad);
        ChunkEvent.Unload.BUS.addListener(ForgeEvents::onChunkUnload);
    }

    private static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        ModLifecycle.registerKeyMappings(event::register);
        // Forge has no event for debug screen entries
        DebugScreenExtensions.register(DebugScreenEntries::register);
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