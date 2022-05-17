package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;

public class SpeedCounterController {

    public static final SpeedCounterController instance = new SpeedCounterController();

    private static long INTERVAL = 500 * 1000000; // 0.5s

    private final Minecraft mc = Minecraft.getInstance();
    private final LinkedList<Entry> list = new LinkedList<>();

    private SpeedCounterController() {

    }

    public double getSpeed() {
        if (list.size() >= 2) {
            Entry first = list.getFirst();
            Entry last = list.getLast();
            double dx = last.x - first.x;
            double dz = last.z - first.z;
            return 1e9 * Math.sqrt(dx * dx + dz * dz) / (last.time - first.time);
        } else {
            return 0;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null) {
            list.clear();
            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            long now = System.nanoTime();
            var entry = new Entry();
            entry.time = now;
            entry.x = mc.player.getX();
            entry.z = mc.player.getZ();
            list.addLast(entry);

            while (now - list.getFirst().time > INTERVAL) {
                list.removeFirst();
            }
        }
    }

    private static class Entry {
        public long time;
        public double x;
        public double z;
    }

}
