package com.zergatul.cheatutils.configs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class KillAuraConfig {

    public boolean active;
    public float maxRange;
    public List<PriorityEntry> priorities;
    public boolean useCrits;

    public JsonKillAuraConfig convert() {
        var config = new JsonKillAuraConfig();
        config.active = active;
        config.maxRange = maxRange;
        return config;
    }

    public static KillAuraConfig createDefault() {
        var config = new KillAuraConfig();
        config.maxRange = 5;
        config.priorities = new ArrayList<>();
        config.priorities.add(PriorityEntry.shulkerBullets);
        config.priorities.add(PriorityEntry.monsters);
        config.priorities.add(PriorityEntry.phantoms);
        config.priorities.add(PriorityEntry.shulkers);
        return config;
    }

    public static class PriorityEntry {

        public static final PriorityEntry monsters = new PriorityEntry("Monsters", Monster.class, e -> {
            if (e instanceof EnderMan) {
                return ((EnderMan) e).isCreepy();
            }
            return true;
        });

        public static final PriorityEntry phantoms = new PriorityEntry("Phantoms", Phantom.class);

        public static final PriorityEntry shulkers = new PriorityEntry("Shulkers", Shulker.class, e -> {
            //var shulker = (Shulker) e;
            //return shulker.isClosed();
            return true;
        });

        public static final PriorityEntry shulkerBullets = new PriorityEntry("Shulker Bullets", ShulkerBullet.class, false);

        public final String name;
        public final Class clazz;
        public final Predicate<Entity> predicate;
        public final boolean waitForCooldown;

        public PriorityEntry(String name, Class clazz) {
            this.name = name;
            this.clazz = clazz;
            this.predicate = null;
            this.waitForCooldown = true;
        }

        public PriorityEntry(String name, Class clazz, Predicate<Entity> predicate) {
            this.name = name;
            this.clazz = clazz;
            this.predicate = predicate;
            this.waitForCooldown = true;
        }

        public PriorityEntry(String name, Class clazz, boolean waitForCooldown) {
            this.name = name;
            this.clazz = clazz;
            this.predicate = null;
            this.waitForCooldown = waitForCooldown;
        }
    }
}
