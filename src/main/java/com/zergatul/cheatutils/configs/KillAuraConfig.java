package com.zergatul.cheatutils.configs;


import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;

import java.util.*;
import java.util.function.Predicate;

public class KillAuraConfig implements ValidatableConfig {

    public boolean active;
    public float maxRange;
    public int attackTickInterval;
    public Double maxHorizontalAngle;
    public Double maxVerticalAngle;
    public ImmutableList<PriorityEntry> priorities;
    public ImmutableList<CustomPriorityEntry> customEntries;

    public KillAuraConfig() {
        active = false;
        maxRange = 6;
        attackTickInterval = 5;
        priorities = new ImmutableList<PriorityEntry>()
                .add(PredefinedPriorityEntry.fromName("Enemies"))
                .add(PredefinedPriorityEntry.fromName("Shulker Bullets"))
                .add(PredefinedPriorityEntry.fromName("Fireballs"));
        customEntries = new ImmutableList<>();
    }

    public void validate() {
        maxRange = MathUtils.clamp(maxRange, 1, 100);
        attackTickInterval = MathUtils.clamp(attackTickInterval, 1, 100);
        if (maxHorizontalAngle != null) {
            maxHorizontalAngle = MathUtils.clamp(maxHorizontalAngle, 1, 180);
        }
        if (maxVerticalAngle != null) {
            maxVerticalAngle = MathUtils.clamp(maxVerticalAngle, 1, 180);
        }

        priorities = priorities.removeIf(Objects::isNull);
        customEntries = customEntries.removeIf(Objects::isNull);
    }

    public static class PriorityEntry {
        public final String name;
        public final String description;
        public final Predicate<Entity> predicate;
        public boolean enabled;

        public PriorityEntry(String name, String description, Predicate<Entity> predicate) {
            this.name = name;
            this.description = description;
            this.predicate = predicate;
            this.enabled = true;
        }
    }

    public static class PredefinedPriorityEntry extends PriorityEntry {

        public static final String ENEMIES = "Enemies";
        public static final String ENEMIES_WO_PIGLINS = "Enemies w/o Piglins";
        public static final String PLAYERS = "Players";
        public static final String SHULKER_BULLETS = "Shulker Bullets";
        public static final String FIREBALLS = "Fireballs";

        public static final Map<String, PredefinedPriorityEntry> entries = Map.ofEntries(
                Map.entry(ENEMIES, new PredefinedPriorityEntry(
                        ENEMIES,
                        "Includes Monsters, Slimes, Magma Cubes, Hoglin. Excludes Neutral Mobs (Enderman, ZombifiedPiglin). Endermen can be targeted only in creepy state.",
                        entity -> {
                            if (entity instanceof EndermanEntity) {
                                return ((EndermanEntity) entity).isAngry();
                            }
                            return entity instanceof Monster && !(entity instanceof Angerable);
                        })),
                Map.entry(ENEMIES_WO_PIGLINS, new PredefinedPriorityEntry(
                        ENEMIES_WO_PIGLINS,
                        "Same as Enemies, but without Piglins.",
                        entity -> {
                            if (entity instanceof EndermanEntity) {
                                return ((EndermanEntity) entity).isAngry();
                            }
                            if (entity instanceof AbstractPiglinEntity) {
                                return false;
                            }
                            return entity instanceof Monster && !(entity instanceof Angerable);
                        }
                )),
                Map.entry(PLAYERS, new PredefinedPriorityEntry(PLAYERS, null, entity -> entity instanceof OtherClientPlayerEntity)),
                Map.entry(SHULKER_BULLETS, new PredefinedPriorityEntry(SHULKER_BULLETS, null, entity -> entity instanceof ShulkerBulletEntity)),
                Map.entry(FIREBALLS, new PredefinedPriorityEntry(FIREBALLS, "Ghast projectiles.", entity -> entity instanceof FireballEntity))
        );

        private PredefinedPriorityEntry(String name, String description, Predicate<Entity> predicate) {
            super(name, description, predicate);
        }

        public static PredefinedPriorityEntry fromName(String name) {
            return entries.get(name);
        }
    }

    public static class CustomPriorityEntry extends PriorityEntry {

        public final String className;

        private CustomPriorityEntry(String name, String description, Predicate<Entity> predicate, String className) {
            super(name, description, predicate);
            this.className = className;
        }

        public static CustomPriorityEntry create(String name, String description, String className) {
            Class<?> clazz;
            try {
                clazz = Class.forName(ClassRemapper.toObf(className));
            } catch (ClassNotFoundException e) {
                return null;
            }
            return new CustomPriorityEntry(name, description, entity -> clazz.isInstance(entity), className);
        }
    }
}