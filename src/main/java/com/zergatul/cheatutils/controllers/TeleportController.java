package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class TeleportController {

    public static final TeleportController instance = new TeleportController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private RegistryKey<World> dimension;

    private TeleportController() {
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
        ModApiWrapper.ClientPlayerLoggingOut.add(this::onPlayerLoggingOut);
    }

    private void onClientTickEnd() {
        if (mc.player != null && mc.world != null) {
            if (dimension == null) {
                dimension = mc.world.getRegistryKey();
            } else {
                if (mc.world.getRegistryKey() != dimension) {
                    ChunkController.instance.clear();
                    BlockFinderController.instance.clear();
                    dimension = mc.world.getRegistryKey();
                }
            }
        }
    }

    private void onPlayerLoggingOut() {
        dimension = null;
    }
}