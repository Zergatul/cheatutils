package com.zergatul.cheatutils.mixins.common.schematics;

import com.mojang.blaze3d.vertex.VertexSorting;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.extensions.SectionCompileInfo;
import com.zergatul.cheatutils.schematics.extensions.SectionCompilerExtension;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {

    @Inject(at = @At("HEAD"), method = "compile")
    private void onCompileBegin(
            SectionPos sectionPos,
            RenderChunkRegion renderChunkRegion,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack sectionBufferBuilderPack,
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

    @Inject(at = @At("TAIL"), method = "compile")
    private void onCompileEnd(
            SectionPos sectionPos,
            RenderChunkRegion renderChunkRegion,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack sectionBufferBuilderPack,
            CallbackInfoReturnable<SectionCompiler.Results> info
    ) {
        SectionCompilerExtension.COMPILE_INFO.get().clear();
    }
}