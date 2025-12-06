package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.extensions.ChunkRenderContextExtension;
import com.zergatul.cheatutils.extensions.LevelSliceExtension;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import com.zergatul.mixin.LocalVariable;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelSlice.class, remap = false)
public abstract class MixinLevelSlice implements LevelSliceExtension {

    @Shadow
    public static int getLocalSectionIndex(int sectionX, int sectionY, int sectionZ) {
        throw new AssertionError();
    }

    @Shadow
    public static int getLocalBlockIndex(int blockX, int blockY, int blockZ) {
        throw new AssertionError();
    }

    @Shadow
    private BoundingBox volume;

    @Shadow
    private BlockState[][] blockArrays;

    @Shadow
    private int originBlockX;

    @Shadow
    private int originBlockY;

    @Shadow
    private int originBlockZ;

    @Unique
    private SchematicaSectionCopy[] schematicaSections_CU;

    public boolean hasSchematicaBlockAt_CU(int x, int y, int z) {
        if (this.schematicaSections_CU == null) {
            return false;
        }

        if (!this.volume.isInside(x, y, z)) {
            return false;
        }

        int relBlockX = x - this.originBlockX;
        int relBlockY = y - this.originBlockY;
        int relBlockZ = z - this.originBlockZ;

        //SchematicaSectionCopy section = this.schematicaSections_CU[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];
        BlockState blockState = this.blockArrays
                [getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)]
                [getLocalBlockIndex(x & 15, y & 15, z & 15)];
        return blockState.isAir();
    }

    @ModifyMethodReturnValue(method = "prepare", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
    private static boolean onModifyHasOnlyAir(boolean hasOnlyAir, @LocalVariable(ordinal = 0) SectionPos pos) {
        if (!hasOnlyAir) {
            return false;
        }

        return !Schematica.instance.hasBlocksAtSection(pos.getX(), pos.getY(), pos.getZ());
    }

    @Inject(at = @At("RETURN"), method = "prepare")
    private static void onModifyChunkRenderContext(Level level, SectionPos pos, ClonedChunkSectionCache cache, CallbackInfoReturnable<ChunkRenderContext> info) {
        if (info.getReturnValue() == null) {
            return;
        }

        Schematica schematica = Schematica.instance;
        if (!schematica.isBlockRenderingEnabled()) {
            return;
        }

        int xs = pos.getX();
        int ys = pos.getY();
        int zs = pos.getZ();

        int xs1 = xs - RenderSectionRegion.RADIUS;
        int xs2 = xs + RenderSectionRegion.RADIUS;
        int ys1 = ys - RenderSectionRegion.RADIUS;
        int ys2 = ys + RenderSectionRegion.RADIUS;
        int zs1 = zs - RenderSectionRegion.RADIUS;
        int zs2 = zs + RenderSectionRegion.RADIUS;

        boolean hasSchematicaBlocks = false;
        for (int x = xs1; x <= xs2; x++) {
            for (int y = ys1; y <= ys2; y++) {
                for (int z = zs1; z <= zs2; z++) {
                    if (schematica.hasBlocksAtSection(x, y, z)) {
                        hasSchematicaBlocks = true;
                    }
                }
            }
        }

        if (!hasSchematicaBlocks) {
            return;
        }

        SchematicaSectionCopy[] sections = new SchematicaSectionCopy[27];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    // TODO: cache copies like Sodium does
                    sections[getLocalSectionIndex(x, y, z)] = schematica.createSectionCopy(SectionPos.asLong(xs1 + x, ys1 + y, zs1 + z));
                }
            }
        }

        ((ChunkRenderContextExtension) info.getReturnValue()).setSchematicaSections_CU(sections);
    }

    @Inject(at = @At("TAIL"), method = "copyData")
    private void onCopyData(ChunkRenderContext context, CallbackInfo info) {
        this.schematicaSections_CU = ((ChunkRenderContextExtension) context).getSchematicaSections_CU();
    }

    @Inject(
            at = @At("RETURN"),
            method = "Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;",
            cancellable = true)
    private void onGetBlockState(int blockX, int blockY, int blockZ, CallbackInfoReturnable<BlockState> info) {
        if (this.schematicaSections_CU == null) {
            return;
        }

        if (!info.getReturnValue().isAir()) {
            return;
        }

        int index = getLocalSectionIndex(
                SectionPos.blockToSectionCoord(blockX - originBlockX),
                SectionPos.blockToSectionCoord(blockY - originBlockY),
                SectionPos.blockToSectionCoord(blockZ - originBlockZ));
        info.setReturnValue(this.schematicaSections_CU[index].getBlockState(blockX, blockY, blockZ));
    }
}