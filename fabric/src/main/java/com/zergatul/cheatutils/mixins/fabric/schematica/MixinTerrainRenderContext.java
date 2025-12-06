package com.zergatul.cheatutils.mixins.fabric.schematica;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.schematics.ShadedVertexConsumerWrapper;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractTerrainRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(TerrainRenderContext.class)
public abstract class MixinTerrainRenderContext extends AbstractTerrainRenderContext {

    @Unique
    private BlockPos currentBlockPos_CU;

    @Inject(method = "bufferModel", at = @At("HEAD"))
    private void onBeforeBufferModel(BlockStateModel model, BlockState blockState, BlockPos blockPos, CallbackInfo info) {
        currentBlockPos_CU = blockPos;
    }

    @Inject(method = "bufferModel", at = @At("TAIL"))
    private void onAfterBufferModel(BlockStateModel model, BlockState blockState, BlockPos blockPos, CallbackInfo info) {
        currentBlockPos_CU = null;
    }

    @ModifyMethodReturnValue(
            method = "getVertexConsumer",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
    private VertexConsumer onOverrideVertexConsumer(VertexConsumer consumer) {
        if (((RenderSectionRegionExtension) this.blockInfo.blockView).hasSchematicaBlockAt_CU(currentBlockPos_CU)) {
            return new ShadedVertexConsumerWrapper(consumer, 0.5f, 0.8f, 1.0f, 0.6f);
        } else {
            return consumer;
        }
    }
}