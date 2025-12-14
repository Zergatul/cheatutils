package com.zergatul.cheatutils.mixins.fabric.schematica;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.schematics.ShadedVertexConsumerWrapper;
import com.zergatul.mixin.LocalVariable;
import com.zergatul.mixin.ModifyArgument;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {

    @ModifyArgument(
            method = "compile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLjava/util/List;)V"))
    private VertexConsumer onModifyVertexConsumer(
            VertexConsumer consumer,
            @LocalVariable(ordinal = 0) RenderSectionRegion region,
            @LocalVariable(ordinal = 2) BlockPos pos
    ) {
        if (((RenderSectionRegionExtension) region).hasSchematicaBlockAt_CU(pos)) {
            return new ShadedVertexConsumerWrapper(consumer, 0.5f, 0.8f, 1.0f, 0.6f);
        } else {
            return consumer;
        }
    }
}