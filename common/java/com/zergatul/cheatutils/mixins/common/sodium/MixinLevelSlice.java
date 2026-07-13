package com.zergatul.cheatutils.mixins.common.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.SodiumLevelSliceExtension;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelSlice.class)
public abstract class MixinLevelSlice implements SodiumLevelSliceExtension {

    @Shadow
    @Final
    private BlockState[][] blockArrays;

    @Shadow
    private int originBlockX;

    @Shadow
    private int originBlockY;

    @Shadow
    private int originBlockZ;

    @Shadow
    private BoundingBox volume;

    @Unique
    private SchematicaSectionCopy[] schematicaSections_CU;

    @Unique
    private int schematicaSectionArrayLength_CU;

    @Unique
    private boolean schematicaView_CU;

    @Unique
    private boolean schematicaShadeBlocks_CU;

    @ModifyExpressionValue(
            method = "prepare",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
    private static boolean onCheckSectionEmpty(boolean original, @Local(argsOnly = true) SectionPos pos) {
        Schematica schematica = Schematica.instance;
        return original && (!schematica.isBlockRenderingEnabled() || !schematica.hasBlocksAtSection(pos.asLong()));
    }

    @Inject(method = "copyData", at = @At("TAIL"))
    private void onCopyData(CallbackInfo info) {
        this.schematicaSections_CU = null;
        this.schematicaSectionArrayLength_CU = 0;
        this.schematicaView_CU = false;
        this.schematicaShadeBlocks_CU = false;

        Schematica schematica = Schematica.instance;
        if (!schematica.isBlockRenderingEnabled()) {
            return;
        }

        int sectionArrayLength = Math.round((float) Math.cbrt(this.blockArrays.length));
        if (sectionArrayLength * sectionArrayLength * sectionArrayLength != this.blockArrays.length) {
            return;
        }

        SchematicaSectionCopy[] sections = null;

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
                    if (sections == null) {
                        sections = new SchematicaSectionCopy[this.blockArrays.length];
                    }
                    sections[sectionIndex] = schematica.createSectionCopy(sectionPos);
                }
            }
        }

        this.schematicaSections_CU = sections;
        this.schematicaSectionArrayLength_CU = sectionArrayLength;
        this.schematicaShadeBlocks_CU = ConfigStore.instance.getConfig().schematicaConfig.shadeBlocks;
    }

    @Inject(method = "getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), cancellable = true)
    private void onGetBlockState(int x, int y, int z, CallbackInfoReturnable<BlockState> info) {
        if (this.schematicaView_CU && info.getReturnValue().isAir()) {
            BlockState state = this.getSchematicaBlockState_CU(x, y, z);
            if (!state.isAir()) {
                info.setReturnValue(state);
            }
        }
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void onReset(CallbackInfo info) {
        this.schematicaSections_CU = null;
        this.schematicaSectionArrayLength_CU = 0;
        this.schematicaView_CU = false;
        this.schematicaShadeBlocks_CU = false;
    }

    @Override
    public boolean hasSchematicaBlocks_CU() {
        return this.schematicaSections_CU != null;
    }

    @Override
    public BlockState getSchematicaBlockState_CU(int x, int y, int z) {
        if (this.schematicaSections_CU == null || this.volume == null || !this.volume.isInside(x, y, z)) {
            return Blocks.AIR.defaultBlockState();
        }

        int relativeX = x - this.originBlockX;
        int relativeY = y - this.originBlockY;
        int relativeZ = z - this.originBlockZ;
        int sectionX = relativeX >> 4;
        int sectionY = relativeY >> 4;
        int sectionZ = relativeZ >> 4;
        int length = this.schematicaSectionArrayLength_CU;
        if (sectionX < 0 || sectionX >= length || sectionY < 0 || sectionY >= length || sectionZ < 0 || sectionZ >= length) {
            return Blocks.AIR.defaultBlockState();
        }

        int index = (sectionY * length * length) + (sectionZ * length) + sectionX;
        SchematicaSectionCopy section = this.schematicaSections_CU[index];
        if (section == null) {
            return Blocks.AIR.defaultBlockState();
        }

        return section.getBlockState(relativeX, relativeY, relativeZ);
    }

    @Override
    public boolean isSchematicaView_CU() {
        return this.schematicaView_CU;
    }

    @Override
    public void setSchematicaView_CU(boolean value) {
        this.schematicaView_CU = value;
    }

    @Override
    public boolean shadeSchematicaBlocks_CU() {
        return this.schematicaShadeBlocks_CU;
    }
}