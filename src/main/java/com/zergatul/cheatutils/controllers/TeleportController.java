package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class TeleportController {

    public static final TeleportController instance = new TeleportController();

    private final Minecraft mc = Minecraft.getInstance();
    private ResourceKey<Level> dimension;

    private TeleportController() {
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
        ModApiWrapper.ClientPlayerLoggingOut.add(this::onPlayerLoggingOut);
    }

    private void onClientTickEnd() {
        if (mc.player != null && mc.level != null) {
            if (dimension == null) {
                dimension = mc.level.dimension();
            } else {
                if (mc.level.dimension() != dimension) {
                    ChunkController.instance.clear();
                    BlockFinderController.instance.clear();
                    dimension = mc.level.dimension();
                    ModApiWrapper.DimensionChange.trigger();
                }
            }
        }
    }

    private void onPlayerLoggingOut() {
        dimension = null;
    }
}