package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.Arrays;
import java.util.Objects;

public class CrystalAuraConfig extends ModuleConfig implements Sanitizable {

    public static final String HOTBAR_SWITCH_NORMAL = "NORMAL";
    public static final String HOTBAR_SWITCH_SILENT = "SILENT";

    public double minTargetDamage;
    public double maxSelfDamage;
    public String hotbarSwitchMode;
    public boolean pauseOnItemUse;
    public boolean pauseOnMining;
    public EntityType<?>[] targets;

    public boolean autoRotate;

    public boolean autoPlace;
    public double placeRange;
    public int placeDelay;

    public boolean autoPlaceSupport;
    public boolean airPlace;
    public int placeSupportDelay;

    public boolean autoBreak;
    public double breakRange;
    public int breakDelay;
    public boolean fastBreak;
    public int crystalAge;

    public CrystalAuraConfig() {
        minTargetDamage = 6;
        maxSelfDamage = 6;
        hotbarSwitchMode = HOTBAR_SWITCH_SILENT;
        pauseOnItemUse = true;
        pauseOnMining = true;
        targets = new EntityType[] {EntityTypes.PLAYER };

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
        if (!HOTBAR_SWITCH_NORMAL.equals(hotbarSwitchMode) && !HOTBAR_SWITCH_SILENT.equals(hotbarSwitchMode)) {
            hotbarSwitchMode = HOTBAR_SWITCH_SILENT;
        }

        if (targets == null) {
            targets = new EntityType[0];
        }
        targets = Arrays.stream(targets).filter(Objects::nonNull).toArray(EntityType[]::new);

        placeDelay = MathUtils.clamp(placeDelay, 0, 20);
        placeRange = MathUtils.clamp(placeRange, 0, 10);

        placeSupportDelay = MathUtils.clamp(placeSupportDelay, 0, 20);

        breakDelay = MathUtils.clamp(breakDelay, 0, 20);
        breakRange = MathUtils.clamp(breakRange, 0, 10);
        crystalAge = MathUtils.clamp(crystalAge, 0, 20);
    }
}