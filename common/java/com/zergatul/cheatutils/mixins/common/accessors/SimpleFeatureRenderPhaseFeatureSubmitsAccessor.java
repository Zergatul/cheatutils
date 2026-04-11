package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(SimpleFeatureRenderPhase.FeatureSubmits.class)
public interface SimpleFeatureRenderPhaseFeatureSubmitsAccessor {

    @Accessor("unbatched")
    List<SubmitNode> getUnbatched_CU();

    @Accessor("batches")
    Map<Object, List<SubmitNode>> getBatches_CU();
}