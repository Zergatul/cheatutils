package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;

import java.util.LinkedList;

public class SpeedCounterController {

    public static final SpeedCounterController instance = new SpeedCounterController();

    private static long INTERVAL = 500 * 1000000; // 0.5s

    private final Minecraft mc = Minecraft.getInstance();
    private final LinkedList<Entry> list = new LinkedList<>();

    private SpeedCounterController() {
        ModApiWrapper.addOnClientTickEnd(this::onClientTickEnd);
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

    private void onClientTickEnd() {
        if (mc.player == null) {
            list.clear();
            return;
        }

        long now = System.nanoTime();
        Entry entry = new Entry();
        entry.time = now;
        entry.x = mc.player.getX();
        entry.z = mc.player.getZ();
        list.addLast(entry);

        while (now - list.getFirst().time > INTERVAL) {
            list.removeFirst();
        }
    }

    private static class Entry {
        public long time;
        public double x;
        public double z;
    }
}