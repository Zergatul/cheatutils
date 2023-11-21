package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public interface CompositeRenderTypeAccessor {

    @Accessor("state")
    RenderType.CompositeState getState_CU();
}