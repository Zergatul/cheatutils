package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.Objects;

public record CrystalAuraConfig(
        boolean enabled,
        double minTargetDamage,
        double maxSelfDamage,
        String hotbarSwitchMode,
        boolean pauseOnItemUse,
        boolean pauseOnMining,
        ImmutableList<EntityType<?>> targets,
        boolean autoRotate,
        boolean autoPlace,
        double placeRange,
        int placeDelay,
        boolean autoPlaceSupport,
        boolean airPlace,
        int placeSupportDelay,
        boolean autoBreak,
        double breakRange,
        int breakDelay,
        boolean fastBreak,
        int crystalAge
) implements ImmutableConfig, ToggleableConfig<CrystalAuraConfig> {

    public static final String HOTBAR_SWITCH_NORMAL = "NORMAL";
    public static final String HOTBAR_SWITCH_SILENT = "SILENT";

    public CrystalAuraConfig {
        minTargetDamage = MathUtils.clamp(minTargetDamage, 0, 100);
        maxSelfDamage = MathUtils.clamp(maxSelfDamage, 0, 100);
        if (!HOTBAR_SWITCH_NORMAL.equals(hotbarSwitchMode) && !HOTBAR_SWITCH_SILENT.equals(hotbarSwitchMode)) {
            hotbarSwitchMode = HOTBAR_SWITCH_SILENT;
        }

        if (targets == null) {
            targets = ImmutableList.empty();
        }
        targets = targets.removeIf(Objects::isNull);

        placeDelay = MathUtils.clamp(placeDelay, 0, 20);
        placeRange = MathUtils.clamp(placeRange, 0, 10);

        placeSupportDelay = MathUtils.clamp(placeSupportDelay, 0, 20);

        breakDelay = MathUtils.clamp(breakDelay, 0, 20);
        breakRange = MathUtils.clamp(breakRange, 0, 10);
        crystalAge = MathUtils.clamp(crystalAge, 0, 20);
    }

    public static CrystalAuraConfig defaults() {
        return new Builder()
                .enabled(false)
                .minTargetDamage(6)
                .maxSelfDamage(6)
                .hotbarSwitchMode(HOTBAR_SWITCH_SILENT)
                .pauseOnItemUse(true)
                .pauseOnMining(true)
                .targets(ImmutableList.from(EntityTypes.PLAYER))
                .autoRotate(false)
                .autoPlace(true)
                .placeRange(4.5)
                .placeDelay(1)
                .autoPlaceSupport(true)
                .airPlace(false)
                .placeSupportDelay(1)
                .autoBreak(true)
                .breakRange(4.5)
                .breakDelay(1)
                .fastBreak(true)
                .crystalAge(0)
                .build();
    }

    // <generated-code>

    public CrystalAuraConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public CrystalAuraConfig withMinTargetDamage(double minTargetDamage) {
        return Builder.from(this).minTargetDamage(minTargetDamage).build();
    }

    public CrystalAuraConfig withMaxSelfDamage(double maxSelfDamage) {
        return Builder.from(this).maxSelfDamage(maxSelfDamage).build();
    }

    public CrystalAuraConfig withHotbarSwitchMode(String hotbarSwitchMode) {
        return Builder.from(this).hotbarSwitchMode(hotbarSwitchMode).build();
    }

    public CrystalAuraConfig withPauseOnItemUse(boolean pauseOnItemUse) {
        return Builder.from(this).pauseOnItemUse(pauseOnItemUse).build();
    }

    public CrystalAuraConfig withPauseOnMining(boolean pauseOnMining) {
        return Builder.from(this).pauseOnMining(pauseOnMining).build();
    }

    public CrystalAuraConfig withTargets(ImmutableList<EntityType<?>> targets) {
        return Builder.from(this).targets(targets).build();
    }

    public CrystalAuraConfig withAutoRotate(boolean autoRotate) {
        return Builder.from(this).autoRotate(autoRotate).build();
    }

    public CrystalAuraConfig withAutoPlace(boolean autoPlace) {
        return Builder.from(this).autoPlace(autoPlace).build();
    }

    public CrystalAuraConfig withPlaceRange(double placeRange) {
        return Builder.from(this).placeRange(placeRange).build();
    }

    public CrystalAuraConfig withPlaceDelay(int placeDelay) {
        return Builder.from(this).placeDelay(placeDelay).build();
    }

    public CrystalAuraConfig withAutoPlaceSupport(boolean autoPlaceSupport) {
        return Builder.from(this).autoPlaceSupport(autoPlaceSupport).build();
    }

    public CrystalAuraConfig withAirPlace(boolean airPlace) {
        return Builder.from(this).airPlace(airPlace).build();
    }

    public CrystalAuraConfig withPlaceSupportDelay(int placeSupportDelay) {
        return Builder.from(this).placeSupportDelay(placeSupportDelay).build();
    }

    public CrystalAuraConfig withAutoBreak(boolean autoBreak) {
        return Builder.from(this).autoBreak(autoBreak).build();
    }

    public CrystalAuraConfig withBreakRange(double breakRange) {
        return Builder.from(this).breakRange(breakRange).build();
    }

    public CrystalAuraConfig withBreakDelay(int breakDelay) {
        return Builder.from(this).breakDelay(breakDelay).build();
    }

    public CrystalAuraConfig withFastBreak(boolean fastBreak) {
        return Builder.from(this).fastBreak(fastBreak).build();
    }

    public CrystalAuraConfig withCrystalAge(int crystalAge) {
        return Builder.from(this).crystalAge(crystalAge).build();
    }

    public static class Builder {

        private boolean enabled;
        private double minTargetDamage;
        private double maxSelfDamage;
        private String hotbarSwitchMode;
        private boolean pauseOnItemUse;
        private boolean pauseOnMining;
        private ImmutableList<EntityType<?>> targets;
        private boolean autoRotate;
        private boolean autoPlace;
        private double placeRange;
        private int placeDelay;
        private boolean autoPlaceSupport;
        private boolean airPlace;
        private int placeSupportDelay;
        private boolean autoBreak;
        private double breakRange;
        private int breakDelay;
        private boolean fastBreak;
        private int crystalAge;

        private Builder() {}

        public static Builder from(CrystalAuraConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .minTargetDamage(config.minTargetDamage)
                    .maxSelfDamage(config.maxSelfDamage)
                    .hotbarSwitchMode(config.hotbarSwitchMode)
                    .pauseOnItemUse(config.pauseOnItemUse)
                    .pauseOnMining(config.pauseOnMining)
                    .targets(config.targets)
                    .autoRotate(config.autoRotate)
                    .autoPlace(config.autoPlace)
                    .placeRange(config.placeRange)
                    .placeDelay(config.placeDelay)
                    .autoPlaceSupport(config.autoPlaceSupport)
                    .airPlace(config.airPlace)
                    .placeSupportDelay(config.placeSupportDelay)
                    .autoBreak(config.autoBreak)
                    .breakRange(config.breakRange)
                    .breakDelay(config.breakDelay)
                    .fastBreak(config.fastBreak)
                    .crystalAge(config.crystalAge);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder minTargetDamage(double minTargetDamage) {
            this.minTargetDamage = minTargetDamage;
            return this;
        }

        public Builder maxSelfDamage(double maxSelfDamage) {
            this.maxSelfDamage = maxSelfDamage;
            return this;
        }

        public Builder hotbarSwitchMode(String hotbarSwitchMode) {
            this.hotbarSwitchMode = hotbarSwitchMode;
            return this;
        }

        public Builder pauseOnItemUse(boolean pauseOnItemUse) {
            this.pauseOnItemUse = pauseOnItemUse;
            return this;
        }

        public Builder pauseOnMining(boolean pauseOnMining) {
            this.pauseOnMining = pauseOnMining;
            return this;
        }

        public Builder targets(ImmutableList<EntityType<?>> targets) {
            this.targets = targets;
            return this;
        }

        public Builder autoRotate(boolean autoRotate) {
            this.autoRotate = autoRotate;
            return this;
        }

        public Builder autoPlace(boolean autoPlace) {
            this.autoPlace = autoPlace;
            return this;
        }

        public Builder placeRange(double placeRange) {
            this.placeRange = placeRange;
            return this;
        }

        public Builder placeDelay(int placeDelay) {
            this.placeDelay = placeDelay;
            return this;
        }

        public Builder autoPlaceSupport(boolean autoPlaceSupport) {
            this.autoPlaceSupport = autoPlaceSupport;
            return this;
        }

        public Builder airPlace(boolean airPlace) {
            this.airPlace = airPlace;
            return this;
        }

        public Builder placeSupportDelay(int placeSupportDelay) {
            this.placeSupportDelay = placeSupportDelay;
            return this;
        }

        public Builder autoBreak(boolean autoBreak) {
            this.autoBreak = autoBreak;
            return this;
        }

        public Builder breakRange(double breakRange) {
            this.breakRange = breakRange;
            return this;
        }

        public Builder breakDelay(int breakDelay) {
            this.breakDelay = breakDelay;
            return this;
        }

        public Builder fastBreak(boolean fastBreak) {
            this.fastBreak = fastBreak;
            return this;
        }

        public Builder crystalAge(int crystalAge) {
            this.crystalAge = crystalAge;
            return this;
        }

        public CrystalAuraConfig build() {
            return new CrystalAuraConfig(enabled, minTargetDamage, maxSelfDamage, hotbarSwitchMode, pauseOnItemUse, pauseOnMining, targets, autoRotate, autoPlace, placeRange, placeDelay, autoPlaceSupport, airPlace, placeSupportDelay, autoBreak, breakRange, breakDelay, fastBreak, crystalAge);
        }
    }

    // </generated-code>
}