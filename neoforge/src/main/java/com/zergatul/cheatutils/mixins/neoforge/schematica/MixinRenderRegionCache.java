package com.zergatul.cheatutils.mixins.neoforge.schematica;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.RenderChunkRegionExtension;
import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import com.zergatul.cheatutils.schematics.WrapperRenderChunkRegion;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
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
    private final Long2ObjectMap<Schematica.SectionInfo> schematicaSectionInfoCache_CU = new Long2ObjectOpenHashMap<>();

    @Unique
    private final Long2ObjectMap<SchematicaSectionCopy> schematicaSectionCopyCache_CU = new Long2ObjectOpenHashMap<>();

    @Inject(
            method = "createRegion(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/SectionPos;Z)Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;",
            at = @At("RETURN"),
            cancellable = true)
    private void onCreateRegion(Level level, SectionPos sectionPos, boolean nullForEmpty, CallbackInfoReturnable<RenderChunkRegion> info) {
        RenderChunkRegion region = info.getReturnValue();
        Schematica schematica = Schematica.instance;
        if (!schematica.isBlockRenderingEnabled() || !schematica.hasBlocksAtSection(sectionPos.asLong())) {
            return;
        }

        if (region == null) {
            if (nullForEmpty) {
                region = ((RenderRegionCache) (Object) this).createRegion(level, sectionPos, false);
                info.setReturnValue(region);
            }
            return;
        }

        int minSectionX = sectionPos.x() - WrapperRenderChunkRegion.RADIUS;
        int minSectionY = sectionPos.y() - WrapperRenderChunkRegion.RADIUS;
        int minSectionZ = sectionPos.z() - WrapperRenderChunkRegion.RADIUS;
        int maxSectionX = sectionPos.x() + WrapperRenderChunkRegion.RADIUS;
        int maxSectionY = sectionPos.y() + WrapperRenderChunkRegion.RADIUS;
        int maxSectionZ = sectionPos.z() + WrapperRenderChunkRegion.RADIUS;

        SchematicaSectionCopy[] copies = new SchematicaSectionCopy[WrapperRenderChunkRegion.SIZE * WrapperRenderChunkRegion.SIZE * WrapperRenderChunkRegion.SIZE];
        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int y = minSectionY; y <= maxSectionY; y++) {
                for (int z = minSectionZ; z <= maxSectionZ; z++) {
                    long index = SectionPos.asLong(x, y, z);
                    Schematica.SectionInfo section = schematica.getSectionInfo(SectionPos.of(index));
                    SchematicaSectionCopy copy = SchematicaSectionCopy.EMPTY;
                    if (section != null) {
                        if (schematicaSectionInfoCache_CU.get(index) != section) {
                            schematicaSectionInfoCache_CU.put(index, section);
                            schematicaSectionCopyCache_CU.put(index, schematica.createSectionCopy(index));
                        }
                        copy = schematicaSectionCopyCache_CU.get(index);
                    } else {
                        schematicaSectionInfoCache_CU.remove(index);
                        schematicaSectionCopyCache_CU.remove(index);
                    }

                    int arrayIndex = WrapperRenderChunkRegion.index(
                            minSectionX, minSectionY, minSectionZ, x, y, z);
                    copies[arrayIndex] = copy;
                }
            }
        }

        ((RenderChunkRegionExtension) region).setSchematicaSections_CU(
                copies,
                minSectionX,
                minSectionY,
                minSectionZ,
                ConfigStore.instance.getConfig().schematicaConfig.shadeBlocks);
    }
}