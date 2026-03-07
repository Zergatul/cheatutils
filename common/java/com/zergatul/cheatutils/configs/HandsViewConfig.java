package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.world.phys.Vec3;

public class HandsViewConfig extends ModuleConfig implements Sanitizable {

    public boolean renderArms;
    public Vec3 armsScale;
    public Vec3 armsShift;

    public boolean renderItems;
    public Vec3 itemsScale;
    public Vec3 itemsShift;

    public HandsViewConfig() {
        renderArms = true;
        armsScale = defaultScale();
        armsShift = defaultShift();

        renderItems = true;
        itemsScale = defaultScale();
        itemsShift = defaultShift();
    }

    @Override
    public void sanitize() {
        if (armsScale == null) {
            armsScale = defaultScale();
        }
        if (armsShift == null) {
            armsShift = defaultShift();
        }
        if (itemsScale == null) {
            itemsScale = defaultScale();
        }
        if (itemsShift == null) {
            itemsShift = defaultShift();
        }

        armsScale = sanitizeScale(armsScale);
        armsShift = sanitizeShift(armsShift);
        itemsScale = sanitizeScale(itemsScale);
        itemsShift = sanitizeShift(itemsShift);
    }

    private static Vec3 sanitizeScale(Vec3 scale) {
        return new Vec3(
                MathUtils.clamp(scale.x, 0, 100),
                MathUtils.clamp(scale.y, 0, 100),
                MathUtils.clamp(scale.z, 0, 100));
    }

    private static Vec3 sanitizeShift(Vec3 shift) {
        return new Vec3(
                MathUtils.clamp(shift.x, -10, 10),
                MathUtils.clamp(shift.y, -10, 10),
                MathUtils.clamp(shift.z, -10, 10));
    }

    private static Vec3 defaultScale() {
        return new Vec3(1, 1, 1);
    }

    private static Vec3 defaultShift() {
        return Vec3.ZERO;
    }
}