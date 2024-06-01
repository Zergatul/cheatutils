package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.util.BitStorage;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Heightmap.class)
public interface HeightmapAccessor {

    @Accessor("data")
    BitStorage getData_CU();
}