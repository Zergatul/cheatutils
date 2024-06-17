package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityType.class)
public interface EntityTypeAccessor {

    @Accessor("factory")
    EntityType.EntityFactory<?> getFactory_CU();
}