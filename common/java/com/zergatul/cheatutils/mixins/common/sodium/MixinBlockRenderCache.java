package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.extensions.SodiumBlockRenderCacheExtension;
import me.jellysquid.mods.sodium.client.model.light.data.ArrayLightDataCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockRenderCache.class, remap = false)
public abstract class MixinBlockRenderCache implements SodiumBlockRenderCacheExtension {

    @Shadow(remap = false)
    @Final
    private ArrayLightDataCache lightDataCache;

    @Override
    public void resetLightDataCache_CU(int sectionX, int sectionY, int sectionZ) {
        this.lightDataCache.reset(SectionPos.of(sectionX, sectionY, sectionZ));
    }
}