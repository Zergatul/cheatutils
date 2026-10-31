package com.zergatul.cheatutils.configs.modules;

import net.minecraft.world.entity.Entity;

public record KillAuraClassPriorityEntry(
        String name,
        String description,
        Class<?> type,
        boolean enabled
) implements KillAuraPriorityEntry {

    @Override
    public boolean matches(Entity entity) {
        return type.isInstance(entity);
    }

    @Override
    public KillAuraPriorityEntry withEnabled(boolean enabled) {
        return new KillAuraClassPriorityEntry(name, description, type, enabled);
    }
}