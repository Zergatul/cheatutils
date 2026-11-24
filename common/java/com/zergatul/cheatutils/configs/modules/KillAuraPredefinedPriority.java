package com.zergatul.cheatutils.configs.modules;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;

public enum KillAuraPredefinedPriority {
    ENEMIES("Enemies", "Includes Monsters, Slimes, Magma Cubes, Hoglin. Excludes Neutral Mobs (Enderman, Zombified Piglin). Endermen can be targeted only in creepy state."),
    ENEMIES_WO_PIGLINS("Enemies w/o Piglins", "Same as Enemies, but without Piglins (Piglin Brute is still valid target).");

    final String name;
    final String description;

    KillAuraPredefinedPriority(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public boolean matches(Entity entity) {
        return switch (this) {
            case ENEMIES:
                if (entity instanceof Enderman) {
                    yield ((Enderman) entity).isCreepy();
                }
                yield entity instanceof Enemy && !(entity instanceof NeutralMob);

            case ENEMIES_WO_PIGLINS:
                if (entity instanceof Enderman) {
                    yield ((Enderman) entity).isCreepy();
                }
                if (entity instanceof PiglinBrute) {
                    yield true;
                }
                if (entity instanceof AbstractPiglin) {
                    yield false;
                }
                yield entity instanceof Enemy && !(entity instanceof NeutralMob);
        };
    }
}