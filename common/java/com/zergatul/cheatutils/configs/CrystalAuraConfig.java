package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class CrystalAuraConfig extends ModuleConfig implements Sanitizable {

    public double minTargetDamage;
    public double maxSelfDamage;
    public boolean autoRotate;

    public boolean autoPlace;
    public double placeRange;
    public int placeDelay;

    public boolean autoPlaceSupport;
    public int placeSupportDelay;

    public boolean autoBreak;
    public double breakRange;
    public int breakDelay;
    public boolean fastBreak;
    public int crystalAge;

    public CrystalAuraConfig() {
        minTargetDamage = 6;
        maxSelfDamage = 6;
        autoRotate = false;

        autoPlace = true;
        placeRange = 4.5;
        placeDelay = 1;

        autoPlaceSupport = true;
        placeSupportDelay = 1;

        autoBreak = true;
        breakRange = 4.5;
        breakDelay = 1;
        fastBreak = true;
        crystalAge = 0;
    }

    @Override
    public void sanitize() {
        minTargetDamage = MathUtils.clamp(minTargetDamage, 0, 100);
        maxSelfDamage = MathUtils.clamp(maxSelfDamage, 0, 100);

        placeDelay = MathUtils.clamp(placeDelay, 0, 20);
        placeRange = MathUtils.clamp(placeRange, 0, 10);

        placeSupportDelay = MathUtils.clamp(placeSupportDelay, 0, 20);

        breakDelay = MathUtils.clamp(breakDelay, 0, 20);
        breakRange = MathUtils.clamp(breakRange, 0, 10);
        crystalAge = MathUtils.clamp(crystalAge, 0, 20);
    }
}