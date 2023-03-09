package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FreeCamPath {

    private final FreeCamController freeCam;
    private final List<Entry> entries = new ArrayList<>();

    // add rotation direction? rotation count

    public FreeCamPath(FreeCamController freeCam) {
        this.freeCam = freeCam;
    }

    public void add(double time) {
        if (!freeCam.isActive()) {
            return;
        }

        time = MathUtils.clamp(time, 0, 3600000);
        entries.add(new Entry(
                new Vec3(freeCam.getX(), freeCam.getY(), freeCam.getZ()),
                freeCam.getXRot(),
                freeCam.getYRot(),
                time));
    }

    public void clear() {
        entries.clear();
    }

    public List<Entry> get() {
        return entries;
    }

    public Entry interpolate(double time) {
        for (int i = 1; i < entries.size(); i++) {
            Entry e2 = entries.get(i);
            if (time < e2.time) {
                Entry e1 = entries.get(i - 1);
                double factor = time / e2.time;
                return interpolate(e1, e2, factor);
            } else {
                time -= e2.time;
            }
        }
        return null;
    }

    private Entry interpolate(Entry e1, Entry e2, double factor) {
        Vec3 pos = new Vec3(
                e1.position.x + (e2.position.x - e1.position.x) * factor,
                e1.position.y + (e2.position.y - e1.position.y) * factor,
                e1.position.z + (e2.position.z - e1.position.z) * factor);
        double xRot = e1.xRot + (e2.xRot - e1.xRot) * factor;
        double yRot = e1.yRot + (e2.yRot - e1.yRot) * factor;
        return new Entry(pos, xRot, yRot, 0);
    }

    public record Entry(Vec3 position, double xRot, double yRot, double time) {}
}