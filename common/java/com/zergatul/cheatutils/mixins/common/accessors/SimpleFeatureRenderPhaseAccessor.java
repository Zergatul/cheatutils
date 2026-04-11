package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleFeatureRenderPhase.class)
public interface SimpleFeatureRenderPhaseAccessor {

    @Accessor("submitsByFeature")
    SimpleFeatureRenderPhase.FeatureSubmits<SubmitNode>[] getSubmitsByFeature_CU();
}