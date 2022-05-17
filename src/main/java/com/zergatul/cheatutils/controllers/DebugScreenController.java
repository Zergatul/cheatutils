package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.text.DecimalFormat;

public class DebugScreenController {

    public static final DebugScreenController instance = new DebugScreenController();

    private final Minecraft mc = Minecraft.getInstance();
    private final DecimalFormat format = new DecimalFormat("0.00");

    private DebugScreenController() {

    }

    @SubscribeEvent
    public void onRenderGameOverlayTextEvent(RenderGameOverlayEvent.Text event) {
        if (mc.options.renderDebug) {
            event.getLeft().add("");
            event.getLeft().add("Zergatul Cheat Utils");
            event.getLeft().add("Loaded chunks: " + ChunkController.instance.getLoadedChunksCount());
            event.getLeft().add("Block scanning thread queue size: " + BlockFinderController.instance.getScanningQueueCount());
            event.getLeft().add("Block scanning thread load: " + format.format(BlockFinderController.instance.getScanningThreadLoadPercent()) + "%");
            event.getLeft().add("Horizontal speed: " + format.format(SpeedCounterController.instance.getSpeed()));
        }
    }
}
