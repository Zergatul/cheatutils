package com.zergatul.cheatutils.configs.modules;

import net.minecraft.world.entity.Entity;

public record KillAuraPredefinedPriorityEntry(
        KillAuraPredefinedPriority priority,
        boolean enabled
) implements KillAuraPriorityEntry {

    @Override
    public String name() {
        return priority.name;
    }

    @Override
    public String description() {
        return priority.description;
    }

    @Override
    public boolean matches(Entity entity) {
        return priority.matches(entity);
    }

    @Override
    public KillAuraPriorityEntry withEnabled(boolean enabled) {
        return new KillAuraPredefinedPriorityEntry(priority, enabled);
    }
}