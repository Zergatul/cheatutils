package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TranslucentFeatureRenderPhase.class)
public interface TranslucentFeatureRenderPhaseAccessor {

    @Accessor("submits")
    List<TranslucentSubmit> getSubmits_CU();
}