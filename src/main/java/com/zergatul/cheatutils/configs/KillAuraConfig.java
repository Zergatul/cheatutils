package com.zergatul.cheatutils.configs;


import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

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
            if (entity instanceof ZombifiedPiglinEntity) {
                return false;
            }
            if (entity instanceof EndermanEntity) {
                return ((EndermanEntity) entity).isAngry();
            }
            return true;
        });

        public static final PriorityEntry phantoms = new PriorityEntry("Phantoms", null, PhantomEntity.class);
        public static final PriorityEntry hoglins = new PriorityEntry("Hoglins", null, HoglinEntity.class);
        public static final PriorityEntry flyingMobs = new PriorityEntry("Flying mobs", "Ghasts and Phantoms", FlyingEntity.class);
        public static final PriorityEntry shulkers = new PriorityEntry("Shulkers", null, ShulkerEntity.class);
        public static final PriorityEntry shulkerBullets = new PriorityEntry("Shulker Bullets", null, ShulkerBulletEntity.class);
        public static final PriorityEntry players = new PriorityEntry("Players", null, OtherClientPlayerEntity.class);
        public static final PriorityEntry fireballs = new PriorityEntry("Fireball", "For example Ghast projectiles", FireballEntity.class);
        public static final PriorityEntry magmaCubes = new PriorityEntry("Magma Cubes", null, MagmaCubeEntity.class);

        public final String name;
        public final String description;
        public final Class clazz;
        public final BiPredicate<Entity, ClientPlayerEntity> predicate;

        public PriorityEntry(String name, String description, Class clazz) {
            this(name, description, clazz, null);
        }

        public PriorityEntry(String name, String description, Class clazz, BiPredicate<Entity, ClientPlayerEntity> predicate) {
            this.name = name;
            this.description = description;
            this.clazz = clazz;
            this.predicate = predicate;

            entries.put(name, this);
        }
    }
}
