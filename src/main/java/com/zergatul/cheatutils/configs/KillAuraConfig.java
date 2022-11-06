package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import java.util.*;
import java.util.function.BiPredicate;

public class KillAuraConfig {

    public boolean active;
    public float maxRange;
    public List<PriorityEntry> priorities;
    public int attackTickInterval;

    public KillAuraConfig() {
        active = false;
        maxRange = 6;
        attackTickInterval = 1;
        priorities = new ArrayList<>();
        priorities.add(PriorityEntry.shulkerBullets);
        priorities.add(PriorityEntry.monsters);
        priorities.add(PriorityEntry.phantoms);
        priorities.add(PriorityEntry.shulkers);
    }

    public void validate() {
        maxRange = MathUtils.clamp(maxRange, 1, 100);
        attackTickInterval = MathUtils.clamp(attackTickInterval, 1, 100);
        priorities.removeIf(Objects::isNull);
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
        public static final PriorityEntry fireballs = new PriorityEntry("Fireball", "For example Ghast projectiles", Fireball.class);
        public static final PriorityEntry magmaCubes = new PriorityEntry("Magma Cubes", null, MagmaCube.class);
        public static final PriorityEntry slimes = new PriorityEntry("Slimes", null, Slime.class, (entity, player) -> entity.getClass() == Slime.class);

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