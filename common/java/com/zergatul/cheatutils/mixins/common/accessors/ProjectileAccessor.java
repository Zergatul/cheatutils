package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Projectile.class)
public interface ProjectileAccessor {

    @Accessor("owner")
    EntityReference<Entity> getOwner_CU();
}
