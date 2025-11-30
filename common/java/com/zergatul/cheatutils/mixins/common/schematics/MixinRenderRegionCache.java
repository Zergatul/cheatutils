package com.zergatul.cheatutils.mixins.common.schematics;

import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderRegionCache.class)
public abstract class MixinRenderRegionCache {

    @Unique
    private final Long2ObjectMap<SchematicaSectionCopy> schematicaSectionCopyCache = new Long2ObjectOpenHashMap<>();

    @Inject(at = @At("RETURN"), method = "createRegion")
    private void onExtendRegionCopy(Level level, long index, CallbackInfoReturnable<RenderSectionRegion> info) {
        Schematica schematica = Schematica.instance;
        if (!schematica.isBlockRenderingEnabled()) {
            return;
        }

        int xs = SectionPos.x(index);
        int ys = SectionPos.y(index);
        int zs = SectionPos.z(index);

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

        SchematicaSectionCopy[] copies = new SchematicaSectionCopy[RenderSectionRegion.SIZE * RenderSectionRegion.SIZE * RenderSectionRegion.SIZE];
        for (int x = xs1; x <= xs2; x++) {
            for (int y = ys1; y <= ys2; y++) {
                for (int z = zs1; z <= zs2; z++) {
                    int i = RenderSectionRegion.index(xs1, ys1, zs1, x, y, z);
                    copies[i] = schematicaSectionCopyCache.computeIfAbsent(SectionPos.asLong(x, y, z), schematica::createSectionCopy);
                }
            }
        }

        ((RenderSectionRegionExtension) info.getReturnValue()).setSchematicaSections_CU(copies);
    }
}