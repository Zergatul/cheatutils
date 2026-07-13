package com.zergatul.cheatutils.mixins.common.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelSlice.class)
public abstract class MixinLevelSlice {

    @Shadow
    @Final
    private BlockState[][] blockArrays;

    @Shadow
    private int originBlockX;

    @Shadow
    private int originBlockY;

    @Shadow
    private int originBlockZ;

    @ModifyExpressionValue(
            method = "prepare",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
    private static boolean onCheckSectionEmpty(boolean original, @Local(argsOnly = true) SectionPos pos) {
        Schematica schematica = Schematica.instance;
        return original && (!schematica.isBlockRenderingEnabled() || !schematica.hasBlocksAtSection(pos.asLong()));
    }

    @Inject(method = "copyData", at = @At("TAIL"))
    private void onCopyData(CallbackInfo info) {
        Schematica schematica = Schematica.instance;
        if (!schematica.isBlockRenderingEnabled()) {
            return;
        }

        int sectionArrayLength = Math.round((float) Math.cbrt(this.blockArrays.length));
        if (sectionArrayLength * sectionArrayLength * sectionArrayLength != this.blockArrays.length) {
            return;
        }

        int originSectionX = SectionPos.blockToSectionCoord(this.originBlockX);
        int originSectionY = SectionPos.blockToSectionCoord(this.originBlockY);
        int originSectionZ = SectionPos.blockToSectionCoord(this.originBlockZ);

        for (int sectionY = 0; sectionY < sectionArrayLength; sectionY++) {
            for (int sectionZ = 0; sectionZ < sectionArrayLength; sectionZ++) {
                for (int sectionX = 0; sectionX < sectionArrayLength; sectionX++) {
                    long sectionPos = SectionPos.asLong(
                            originSectionX + sectionX,
                            originSectionY + sectionY,
                            originSectionZ + sectionZ);
                    if (!schematica.hasBlocksAtSection(sectionPos)) {
                        continue;
                    }

                    int sectionIndex = (sectionY * sectionArrayLength * sectionArrayLength) +
                            (sectionZ * sectionArrayLength) + sectionX;
                    this.mergeSchematicaSection_CU(this.blockArrays[sectionIndex], schematica.createSectionCopy(sectionPos));
                }
            }
        }
    }

    @Unique
    private void mergeSchematicaSection_CU(BlockState[] blocks, SchematicaSectionCopy schematica) {
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int index = (y << 8) | (z << 4) | x;
                    if (!blocks[index].isAir()) {
                        continue;
                    }

                    BlockState state = schematica.getBlockState(x, y, z);
                    if (!state.isAir()) {
                        blocks[index] = state;
                    }
                }
            }
        }
    }
}