package com.zergatul.cheatutils.mixins.common.schematics;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderRegionCache.class)
public abstract class MixinRenderRegionCache {

    @Unique
    private final Long2ObjectMap<SchematicaSectionCopy> schematicaSectionCopyCache_CU = new Long2ObjectOpenHashMap<>();

    @Inject(at = @At("RETURN"), method = "createRegion")
    private void onExtendRegionCopy(ClientLevel level, long sectionNode, CallbackInfoReturnable<RenderSectionRegion> info) {
        Schematica schematica = Schematica.instance;
        if (!schematica.isBlockRenderingEnabled()) {
            return;
        }

        int xs = SectionPos.x(sectionNode);
        int ys = SectionPos.y(sectionNode);
        int zs = SectionPos.z(sectionNode);

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
                    copies[i] = schematicaSectionCopyCache_CU.computeIfAbsent(SectionPos.asLong(x, y, z), schematica::createSectionCopy);
                }
            }
        }

        ((RenderSectionRegionExtension) info.getReturnValue()).setSchematicaSections_CU(copies, ConfigStore.instance.getConfig().schematicaConfig.shadeBlocks);
    }
}