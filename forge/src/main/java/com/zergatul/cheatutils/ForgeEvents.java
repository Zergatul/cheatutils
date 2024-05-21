package com.zergatul.cheatutils;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.zergatul.cheatutils.common.Events.*;

@SuppressWarnings("unused")
public class ForgeEvents {

    @SubscribeEvent
    public void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
        List<Component> list = new ArrayList<>();
        GatherTooltipComponents.trigger(new GatherTooltipComponentsEvent(event.getItemStack(), list));
        list.forEach(c -> event.getTooltipElements().add(Either.left(c)));
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            ChunkLoaded.trigger();
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ChunkUnloaded.trigger();
        }
    }
}