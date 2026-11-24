package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;

import java.util.Objects;

public record KillAuraConfig(
        boolean enabled,
        boolean overrideAttackRange,
        double maxRange,
        String delayMode,
        int attackTickInterval,
        double extraTicks,
        Double maxHorizontalAngle,
        Double maxVerticalAngle,
        ImmutableList<KillAuraPriorityEntry> priorities,
        boolean attackAll,
        boolean autoRotate,
        boolean doNotAttackOnItemUse
) implements ImmutableConfig, ToggleableConfig<KillAuraConfig> {

    public static final String ConstDelay = "ConstDelay";
    public static final String Cooldown = "Cooldown";

    public KillAuraConfig {
        maxRange = MathUtils.clamp(maxRange, 1, 100);
        attackTickInterval = MathUtils.clamp(attackTickInterval, 1, 100);
        extraTicks = MathUtils.clamp(extraTicks, -10, 1000);
        if (maxHorizontalAngle != null) {
            maxHorizontalAngle = MathUtils.clamp(maxHorizontalAngle, 1, 180);
        }
        if (maxVerticalAngle != null) {
            maxVerticalAngle = MathUtils.clamp(maxVerticalAngle, 1, 180);
        }

        if (priorities == null) {
            priorities = ImmutableList.empty();
        }

        priorities = priorities.removeAll(Objects::isNull);
        priorities = addPredefinedIfMissing(priorities, KillAuraPredefinedPriority.ENEMIES);
        priorities = addPredefinedIfMissing(priorities, KillAuraPredefinedPriority.ENEMIES_WO_PIGLINS);
    }

    public static KillAuraConfig defaults() {
        return new Builder()
                .enabled(false)
                .overrideAttackRange(false)
                .maxRange(6)
                .delayMode(ConstDelay)
                .attackTickInterval(5)
                .extraTicks(0)
                .maxHorizontalAngle(null)
                .maxVerticalAngle(null)
                .priorities(ImmutableList.<KillAuraPriorityEntry>empty()
                        .add(new KillAuraPredefinedPriorityEntry(KillAuraPredefinedPriority.ENEMIES, true))
                        .add(new KillAuraPredefinedPriorityEntry(KillAuraPredefinedPriority.ENEMIES_WO_PIGLINS, false))
                        .add(new KillAuraClassPriorityEntry("Players", null, RemotePlayer.class, false))
                        .add(new KillAuraClassPriorityEntry("Shulker Bullets", null, ShulkerBullet.class, false))
                        .add(new KillAuraClassPriorityEntry("Fireballs", null, Fireball.class, false)))
                .attackAll(false)
                .autoRotate(true)
                .doNotAttackOnItemUse(false)
                .build();
    }

    private static ImmutableList<KillAuraPriorityEntry> addPredefinedIfMissing(ImmutableList<KillAuraPriorityEntry> priorities, KillAuraPredefinedPriority priority) {
        if (!priorities.contains(e -> e instanceof KillAuraPredefinedPriorityEntry predefined && predefined.priority() == priority)) {
            return priorities.add(new KillAuraPredefinedPriorityEntry(priority, false));
        } else {
            return priorities;
        }
    }

    // <generated-code>

    public KillAuraConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public KillAuraConfig withOverrideAttackRange(boolean overrideAttackRange) {
        return Builder.from(this).overrideAttackRange(overrideAttackRange).build();
    }

    public KillAuraConfig withMaxRange(double maxRange) {
        return Builder.from(this).maxRange(maxRange).build();
    }

    public KillAuraConfig withDelayMode(String delayMode) {
        return Builder.from(this).delayMode(delayMode).build();
    }

    public KillAuraConfig withAttackTickInterval(int attackTickInterval) {
        return Builder.from(this).attackTickInterval(attackTickInterval).build();
    }

    public KillAuraConfig withExtraTicks(double extraTicks) {
        return Builder.from(this).extraTicks(extraTicks).build();
    }

    public KillAuraConfig withMaxHorizontalAngle(Double maxHorizontalAngle) {
        return Builder.from(this).maxHorizontalAngle(maxHorizontalAngle).build();
    }

    public KillAuraConfig withMaxVerticalAngle(Double maxVerticalAngle) {
        return Builder.from(this).maxVerticalAngle(maxVerticalAngle).build();
    }

    public KillAuraConfig withPriorities(ImmutableList<KillAuraPriorityEntry> priorities) {
        return Builder.from(this).priorities(priorities).build();
    }

    public KillAuraConfig withAttackAll(boolean attackAll) {
        return Builder.from(this).attackAll(attackAll).build();
    }

    public KillAuraConfig withAutoRotate(boolean autoRotate) {
        return Builder.from(this).autoRotate(autoRotate).build();
    }

    public KillAuraConfig withDoNotAttackOnItemUse(boolean doNotAttackOnItemUse) {
        return Builder.from(this).doNotAttackOnItemUse(doNotAttackOnItemUse).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean overrideAttackRange;
        private double maxRange;
        private String delayMode;
        private int attackTickInterval;
        private double extraTicks;
        private Double maxHorizontalAngle;
        private Double maxVerticalAngle;
        private ImmutableList<KillAuraPriorityEntry> priorities;
        private boolean attackAll;
        private boolean autoRotate;
        private boolean doNotAttackOnItemUse;

        private Builder() {}

        public static Builder from(KillAuraConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .overrideAttackRange(config.overrideAttackRange)
                    .maxRange(config.maxRange)
                    .delayMode(config.delayMode)
                    .attackTickInterval(config.attackTickInterval)
                    .extraTicks(config.extraTicks)
                    .maxHorizontalAngle(config.maxHorizontalAngle)
                    .maxVerticalAngle(config.maxVerticalAngle)
                    .priorities(config.priorities)
                    .attackAll(config.attackAll)
                    .autoRotate(config.autoRotate)
                    .doNotAttackOnItemUse(config.doNotAttackOnItemUse);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder overrideAttackRange(boolean overrideAttackRange) {
            this.overrideAttackRange = overrideAttackRange;
            return this;
        }

        public Builder maxRange(double maxRange) {
            this.maxRange = maxRange;
            return this;
        }

        public Builder delayMode(String delayMode) {
            this.delayMode = delayMode;
            return this;
        }

        public Builder attackTickInterval(int attackTickInterval) {
            this.attackTickInterval = attackTickInterval;
            return this;
        }

        public Builder extraTicks(double extraTicks) {
            this.extraTicks = extraTicks;
            return this;
        }

        public Builder maxHorizontalAngle(Double maxHorizontalAngle) {
            this.maxHorizontalAngle = maxHorizontalAngle;
            return this;
        }

        public Builder maxVerticalAngle(Double maxVerticalAngle) {
            this.maxVerticalAngle = maxVerticalAngle;
            return this;
        }

        public Builder priorities(ImmutableList<KillAuraPriorityEntry> priorities) {
            this.priorities = priorities;
            return this;
        }

        public Builder attackAll(boolean attackAll) {
            this.attackAll = attackAll;
            return this;
        }

        public Builder autoRotate(boolean autoRotate) {
            this.autoRotate = autoRotate;
            return this;
        }

        public Builder doNotAttackOnItemUse(boolean doNotAttackOnItemUse) {
            this.doNotAttackOnItemUse = doNotAttackOnItemUse;
            return this;
        }

        public KillAuraConfig build() {
            return new KillAuraConfig(enabled, overrideAttackRange, maxRange, delayMode, attackTickInterval, extraTicks, maxHorizontalAngle, maxVerticalAngle, priorities, attackAll, autoRotate, doNotAttackOnItemUse);
        }
    }

    // </generated-code>
}