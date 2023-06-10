package com.zergatul.cheatutils.wrappers;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.common.events.PreRenderGuiOverlayEvent;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.zergatul.cheatutils.common.Events.*;

public class ForgeEvents {

    @SubscribeEvent
    public void onPreRenderGui(net.minecraftforge.client.event.RenderGuiEvent.Pre event) {
        if (RenderWorldLastEvent.last != null) {
            PreRenderGui.trigger(new RenderGuiEvent(event.getGuiGraphics(), RenderWorldLastEvent.last));
        }
    }

    @SubscribeEvent
    public void onPostRenderGui(net.minecraftforge.client.event.RenderGuiEvent.Post event) {
        if (RenderWorldLastEvent.last != null) {
            PostRenderGui.trigger(new RenderGuiEvent(event.getGuiGraphics(), RenderWorldLastEvent.last));
        }
    }

    @SubscribeEvent
    public void onPreRenderGameOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == GuiOverlayManager.findOverlay(VanillaGuiOverlay.PLAYER_LIST.id())) {
            if (PreRenderGuiOverlay.trigger(new PreRenderGuiOverlayEvent(PreRenderGuiOverlayEvent.GuiOverlayType.PLAYER_LIST))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
        List<Component> list = new ArrayList<>();
        GatherTooltipComponents.trigger(new GatherTooltipComponentsEvent(event.getItemStack(), list));
        list.forEach(c -> event.getTooltipElements().add(Either.left(c)));
    }
}