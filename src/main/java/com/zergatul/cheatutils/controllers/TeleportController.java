package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class TeleportController {

    public static final TeleportController instance = new TeleportController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private RegistryKey<World> dimension;

    private TeleportController() {
        ModApiWrapper.addOnClientTickEnd(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player != null && mc.world != null) {
            if (mc.world.getRegistryKey() != dimension) {
                ChunkController.instance.clear();
                BlockFinderController.instance.clear();
                dimension = mc.world.getRegistryKey();
            }
        }
    }
}
