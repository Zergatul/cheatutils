package com.zergatul.cheatutils.configs.modules;

import net.minecraft.world.entity.Entity;

public sealed interface KillAuraPriorityEntry permits KillAuraPredefinedPriorityEntry, KillAuraClassPriorityEntry {
    boolean enabled();
    String name();
    String description();
    boolean matches(Entity entity);
    KillAuraPriorityEntry withEnabled(boolean enabled);
}