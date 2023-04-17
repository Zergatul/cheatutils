package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.Minecraft;

import java.util.LinkedList;

public class SpeedCounterController {

    public static final SpeedCounterController instance = new SpeedCounterController();

    private static long INTERVAL = 500 * 1000000; // 0.5s

    private final Minecraft mc = Minecraft.getInstance();
    private final LinkedList<Entry> list = new LinkedList<>();

    private SpeedCounterController() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    public double getHorizontalSpeed() {
        if (list.size() >= 2) {
            Entry first = list.getFirst();
            Entry last = list.getLast();
            double distance = -first.horizontalDistance; // to exclude first entry
            for (Entry entry: list) {
                distance += entry.horizontalDistance;
            }
            return 1e9 * distance / (last.time - first.time);
        } else {
            return 0;
        }
    }

    public double getSpeed() {
        if (list.size() >= 2) {
            Entry first = list.getFirst();
            Entry last = list.getLast();
            double distance = -first.distance; // to exclude first entry
            for (Entry entry: list) {
                distance += entry.distance;
            }
            return 1e9 * distance / (last.time - first.time);
        } else {
            return 0;
        }
    }

    public void onClientTickEnd() {
        if (mc.player == null) {
            list.clear();
            return;
        }

        long now = System.nanoTime();
        var entry = new Entry();
        entry.time = now;
        entry.x = mc.player.getX();
        entry.y = mc.player.getY();
        entry.z = mc.player.getZ();

        if (list.size() > 0) {
            Entry last = list.getLast();
            entry.distance = entry.getDistanceTo(last);
            entry.horizontalDistance = entry.getHorizontalDistanceTo(last);
        }

        list.addLast(entry);

        while (now - list.getFirst().time > INTERVAL) {
            list.removeFirst();
        }
    }

    private static class Entry {
        public long time;
        public double x;
        public double y;
        public double z;
        public double horizontalDistance;
        public double distance;

        public double getDistanceTo(Entry other) {
            double dx = other.x - x;
            double dy = other.y - y;
            double dz = other.z - z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        public double getHorizontalDistanceTo(Entry other) {
            double dx = other.x - x;
            double dz = other.z - z;
            return Math.sqrt(dx * dx + dz * dz);
        }
    }
}