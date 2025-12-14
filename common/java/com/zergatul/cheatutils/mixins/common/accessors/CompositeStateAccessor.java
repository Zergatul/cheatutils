package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.CompositeState.class)
public interface CompositeStateAccessor {

    @Accessor("textureState")
    RenderStateShard.EmptyTextureStateShard getTextureState_CU();
}