package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.SodiumLevelSliceExtension;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelSlice.class, remap = false)
public abstract class MixinLevelSlice implements SodiumLevelSliceExtension {

    @Shadow(remap = false)
    @Final
    private BlockState[][] blockArrays;

    @Shadow(remap = false)
    private int originBlockX;

    @Shadow(remap = false)
    private int originBlockY;

    @Shadow(remap = false)
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

    @Redirect(
            method = "prepare",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z", remap = true),
            remap = false)
    private static boolean onCheckSectionEmpty(
            LevelChunkSection section,
            Level level,
            SectionPos pos,
            ClonedChunkSectionCache cache
    ) {
        Schematica schematica = Schematica.instance;
        return section.hasOnlyAir() && (!schematica.isBlockRenderingEnabled() || !schematica.hasBlocksAtSection(pos.asLong()));
    }

    @Inject(method = "copyData", at = @At("TAIL"), remap = false)
    private void onCopyData(CallbackInfo info) {
        this.clearSchematicaData_CU();

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

                    if (sections == null) {
                        sections = new SchematicaSectionCopy[this.blockArrays.length];
                    }
                    int index = sectionY * sectionArrayLength * sectionArrayLength + sectionZ * sectionArrayLength + sectionX;
                    sections[index] = schematica.createSectionCopy(sectionPos);
                }
            }
        }

        this.schematicaSections_CU = sections;
        this.schematicaSectionArrayLength_CU = sectionArrayLength;
        this.schematicaShadeBlocks_CU = ConfigStore.instance.getConfig().schematicaConfig.shadeBlocks;
    }

    @Inject(
            target = @Desc(value = "getBlockState", args = { int.class, int.class, int.class }, ret = BlockState.class),
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void onGetBlockState(int x, int y, int z, CallbackInfoReturnable<BlockState> info) {
        if (this.schematicaView_CU && info.getReturnValue().isAir()) {
            BlockState state = this.getSchematicaBlockState_CU(x, y, z);
            if (!state.isAir()) {
                info.setReturnValue(state);
            }
        }
    }

    @Inject(method = "reset", at = @At("TAIL"), remap = false)
    private void onReset(CallbackInfo info) {
        this.clearSchematicaData_CU();
    }

    @Unique
    private void clearSchematicaData_CU() {
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

        int index = sectionY * length * length + sectionZ * length + sectionX;
        SchematicaSectionCopy section = this.schematicaSections_CU[index];
        return section == null ? Blocks.AIR.defaultBlockState() : section.getBlockState(relativeX, relativeY, relativeZ);
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