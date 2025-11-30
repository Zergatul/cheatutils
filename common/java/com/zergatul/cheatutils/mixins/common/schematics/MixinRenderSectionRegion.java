package com.zergatul.cheatutils.mixins.common.schematics;

import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSectionRegion.class)
public abstract class MixinRenderSectionRegion implements RenderSectionRegionExtension {

    @Shadow
    @Final
    private int minSectionX;

    @Shadow
    @Final
    private int minSectionY;

    @Shadow
    @Final
    private int minSectionZ;

    @Shadow protected abstract SectionCopy getSection(int p_406718_, int p_406216_, int p_406392_);

    @Unique
    private SchematicaSectionCopy[] schematicaSections_CU;

    @Override
    public void setSchematicaSections_CU(SchematicaSectionCopy[] schematicaSections) {
        this.schematicaSections_CU = schematicaSections;
    }

    @Override
    public boolean hasSchematicaBlockAt_CU(BlockPos pos) {
        if (schematicaSections_CU == null) {
            return false;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int xs = SectionPos.blockToSectionCoord(x);
        int ys = SectionPos.blockToSectionCoord(y);
        int zs = SectionPos.blockToSectionCoord(z);

        BlockState original = this.getSection(xs, ys, zs).getBlockState(pos);
        return original.isAir();
    }

    @Inject(at = @At("RETURN"), method = "getBlockState", cancellable = true)
    private void onGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> info) {
        if (schematicaSections_CU == null) {
            return;
        }

        if (!info.getReturnValue().isAir()) {
            return;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int xs = SectionPos.blockToSectionCoord(x);
        int ys = SectionPos.blockToSectionCoord(y);
        int zs = SectionPos.blockToSectionCoord(z);

        int i = RenderSectionRegion.index(minSectionX, minSectionY, minSectionZ, xs, ys, zs);
        info.setReturnValue(schematicaSections_CU[i].getBlockState(x, y, z));
    }
}