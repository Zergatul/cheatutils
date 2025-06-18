package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FogRenderer.class)
public interface FogRendererAccessor {

    @Accessor("fogEnabled")
    static boolean isFogEnabled_CU() {
        throw new AssertionError();
    }
}