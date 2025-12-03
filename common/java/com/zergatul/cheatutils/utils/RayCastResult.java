package com.zergatul.cheatutils.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record RayCastResult(Entity entity, Vec3 hit) {}
