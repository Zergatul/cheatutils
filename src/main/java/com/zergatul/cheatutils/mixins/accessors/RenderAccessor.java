package com.zergatul.cheatutils.mixins.accessors;

import net.minecraft.client.renderer.entity.Render;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Render.class)
public interface RenderAccessor {

    @Accessor("renderOutlines")
    boolean getRenderOutlines_CU();
}