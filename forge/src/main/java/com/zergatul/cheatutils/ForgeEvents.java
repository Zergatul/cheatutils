package com.zergatul.cheatutils;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.List;

import static com.zergatul.cheatutils.common.Events.*;

public class ForgeEvents {

    public void register() {
        RenderTooltipEvent.GatherComponents.BUS.addListener(this::onPreRenderTooltip);
        ChunkEvent.Load.BUS.addListener(this::onChunkLoad);
        ChunkEvent.Unload.BUS.addListener(this::onChunkUnload);
    }

    private void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
        List<Component> list = new ArrayList<>();
        GatherTooltipComponents.trigger(new GatherTooltipComponentsEvent(event.getItemStack(), list));
        list.forEach(c -> event.getTooltipElements().add(Either.left(c)));
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            RawChunkLoaded.trigger((LevelChunk) event.getChunk());
        }
    }

    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            RawChunkUnloaded.trigger((LevelChunk) event.getChunk());
        }
    }
}