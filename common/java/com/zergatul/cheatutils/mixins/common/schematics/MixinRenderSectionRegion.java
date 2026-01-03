package com.zergatul.cheatutils.mixins.common.schematics;

import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import com.zergatul.cheatutils.schematics.WrapperRenderSectionRegion;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderSectionRegion.class)
public abstract class MixinRenderSectionRegion implements RenderSectionRegionExtension, BlockAndTintGetter {

    @Shadow
    @Final
    private int minSectionX;

    @Shadow
    @Final
    private int minSectionY;

    @Shadow
    @Final
    private int minSectionZ;

    @Shadow
    @Final
    private Level level;

    @Unique
    private SchematicaSectionCopy[] schematicaSections_CU;

    @Unique
    private boolean schematicaShadeBlocks_CU;

    @Override
    public void setSchematicaSections_CU(SchematicaSectionCopy[] schematicaSections, boolean shade) {
        this.schematicaSections_CU = schematicaSections;
        this.schematicaShadeBlocks_CU = shade;
    }

    @Override
    public boolean hasSchematicaBlocks_CU() {
        return schematicaSections_CU != null;
    }

    @Override
    public boolean shadeSchematicaBlocks_CU() {
        return schematicaShadeBlocks_CU;
    }

    @Override
    public BlockAndTintGetter asWrapped_CU() {
        return new WrapperRenderSectionRegion(level, this, schematicaSections_CU, minSectionX, minSectionY, minSectionZ);
    }
}