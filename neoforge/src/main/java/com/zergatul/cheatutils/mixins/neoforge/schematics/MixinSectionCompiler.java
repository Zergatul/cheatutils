package com.zergatul.cheatutils.mixins.neoforge.schematics;

import com.mojang.blaze3d.vertex.VertexSorting;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.extensions.SectionCompileInfo;
import com.zergatul.cheatutils.schematics.extensions.SectionCompilerExtension;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {

    @Inject(at = @At("HEAD"), method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;")
    private void onCompileBegin(
            SectionPos sectionPos,
            RenderSectionRegion section,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack sectionBufferBuilderPack,
            List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers,
            CallbackInfoReturnable<SectionCompiler.Results> unused
    ) {
        SectionCompileInfo info = SectionCompilerExtension.COMPILE_INFO.get();
        info.sectionPos = sectionPos;
        if (Schematica.instance.isBlockRenderingEnabled()) {
            Schematica.SectionInfo schematicaSectionInfo = Schematica.instance.getSectionInfo(sectionPos);
            if (schematicaSectionInfo != null) {
                info.schematicaSectionInfo = schematicaSectionInfo;
                info.shouldRenderSchematicaGhostBlocks = true;
            } else {
                info.shouldRenderSchematicaGhostBlocks = false;
            }
        } else {
            info.shouldRenderSchematicaGhostBlocks = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;")
    private void onCompileEnd(
            SectionPos sectionPos,
            RenderSectionRegion section,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack sectionBufferBuilderPack,
            List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers,
            CallbackInfoReturnable<SectionCompiler.Results> info
    ) {
        SectionCompilerExtension.COMPILE_INFO.get().clear();
    }
}