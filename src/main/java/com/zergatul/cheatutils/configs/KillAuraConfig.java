package com.zergatul.cheatutils.configs;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class KillAuraConfig {

    public boolean active;
    public float maxRange;
    public List<PriorityEntry> priorities;
    public boolean attackEveryTick;

    public KillAuraConfig() {
        active = false;
        maxRange = 6;
        priorities = new ArrayList<>();
        priorities.add(PriorityEntry.shulkerBullets);
        priorities.add(PriorityEntry.monsters);
        priorities.add(PriorityEntry.phantoms);
        priorities.add(PriorityEntry.shulkers);
    }

    public static class PriorityEntry {

        public static final Map<String, PriorityEntry> entries = new HashMap<>();

        public static final PriorityEntry monsters = new PriorityEntry("Monsters", "All monsters except Zombified Piglin. Endermen can be targeted only in creepy state.", Monster.class, (entity, player) -> {
            if (entity instanceof ZombifiedPiglin) {
                return false;
            }
            if (entity instanceof EnderMan) {
                return ((EnderMan) entity).isCreepy();
            }
            return true;
        });

        public static final PriorityEntry phantoms = new PriorityEntry("Phantoms", null, Phantom.class);
        public static final PriorityEntry hoglins = new PriorityEntry("Hoglins", null, Hoglin.class);
        public static final PriorityEntry flyingMobs = new PriorityEntry("Flying mobs", "Ghasts and Phantoms", FlyingMob.class);
        public static final PriorityEntry shulkers = new PriorityEntry("Shulkers", null, Shulker.class);
        public static final PriorityEntry shulkerBullets = new PriorityEntry("Shulker Bullets", null, ShulkerBullet.class);
        public static final PriorityEntry players = new PriorityEntry("Players", null, RemotePlayer.class);

        public final String name;
        public final String description;
        public final Class clazz;
        public final BiPredicate<Entity, LocalPlayer> predicate;

        public PriorityEntry(String name, String description, Class clazz) {
            this(name, description, clazz, null);
        }

        public PriorityEntry(String name, String description, Class clazz, BiPredicate<Entity, LocalPlayer> predicate) {
            this.name = name;
            this.description = description;
            this.clazz = clazz;
            this.predicate = predicate;

            entries.put(name, this);
        }
    }
}
