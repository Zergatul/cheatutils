package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TeleportController {

    public static final TeleportController instance = new TeleportController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(TeleportController.class);
    private boolean first = true;
    private double x, y, z;
    private ResourceKey<Level> dimension;
    private double minDistance = 100 * 100;

    private TeleportController() {

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            boolean teleportDetected = false;
            if (FreeCamController.instance.isActive()) {
                return;
            }
            if (mc.player != null) {
                double xn = mc.player.getX();
                double yn = mc.player.getY();
                double zn = mc.player.getZ();

                if (first) {
                    first = false;
                } else {
                    if (mc.player.level.dimension() != dimension) {
                        teleportDetected = true;
                    } else {
                        double dx = xn - x;
                        double dy = yn - y;
                        double dz = zn - z;
                        if (dx * dx + dy * dy + dz * dz > minDistance) {
                            teleportDetected = true;
                        }
                    }

                    if (teleportDetected) {
                        ChunkController.instance.clear();
                        BlockFinderController.instance.clear();
                    }
                }

                x = xn;
                y = yn;
                z = zn;
                dimension = mc.player.level.dimension();
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnLoad(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide()) {
            first = true;
        }
    }
}
