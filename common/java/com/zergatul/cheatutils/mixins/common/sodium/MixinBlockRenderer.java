package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.extensions.BlockRendererExtension;
import com.zergatul.mixin.ModifyArgument;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.services.PlatformModelEmitter;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class MixinBlockRenderer implements BlockRendererExtension {

//    @Unique
//    private boolean schematicaShadeMode_CU;
//
//    public void setSchematicaShadeMode_CU(boolean value) {
//        schematicaShadeMode_CU = value;
//    }
//
//    @ModifyArgument(
//            method = "renderModel",
//            at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformModelEmitter;emitModel(Lnet/minecraft/client/renderer/block/model/BlockStateModel;Ljava/util/function/Predicate;Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/caffeinemc/mods/sodium/client/services/PlatformModelEmitter$Bufferer;)V"))
//    private MutableQuadViewImpl onModifyBuffer(MutableQuadViewImpl quad) {
//        if (schematicaShadeMode_CU) {
//            quad.setColor(0, 0x80808080);
//            quad.setColor(1, 0x80808080);
//            quad.setColor(2, 0x80808080);
//            quad.setColor(3, 0x80808080);
//        }
//
//        return quad;
//    }
}