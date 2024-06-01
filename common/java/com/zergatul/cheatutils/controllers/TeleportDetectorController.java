package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class TeleportDetectorController {

    public static final TeleportDetectorController instance = new TeleportDetectorController();

    private final Minecraft mc = Minecraft.getInstance();
    private ResourceKey<Level> dimension;

    private TeleportDetectorController() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
        Events.ClientPlayerLoggingOut.add(this::onPlayerLoggingOut);
    }

    private void onClientTickEnd() {
        if (mc.player != null && mc.level != null) {
            if (dimension == null) {
                dimension = mc.level.dimension();
            } else {
                if (mc.level.dimension() != dimension) {
                    clearChunks();
                    dimension = mc.level.dimension();
                    Events.DimensionChange.trigger();
                }
            }
        }
    }

    private void onPlayerLoggingOut() {
        dimension = null;
        clearChunks();
    }

    private void clearChunks() {
        BlockFinderController.instance.clear();
    }
}