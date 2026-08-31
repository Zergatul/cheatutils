package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.extensions.SodiumBlockRendererExtension;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class MixinBlockRenderer implements SodiumBlockRendererExtension {

    @Unique
    private boolean isShaded_CU;

    @Override
    public void setShaded_CU(boolean shaded) {
        this.isShaded_CU = shaded;
    }

    @Inject(method = "getVertexColors", at = @At("RETURN"), remap = false)
    private void onGetVertexColors(
            BlockRenderContext context,
            ColorProvider<BlockState> colorProvider,
            BakedQuadView quad,
            CallbackInfoReturnable<int[]> info
    ) {
        if (!this.isShaded_CU) {
            return;
        }

        int[] colors = info.getReturnValue();
        if (colors == null) {
            return;
        }
        for (int i = 0; i < colors.length; i++) {
            colors[i] = shadeColor_CU(colors[i]);
        }
    }

    @Unique
    private static int shadeColor_CU(int color) {
        int alpha = Math.round(((color >>> 24) & 0xFF) * 0.6f);
        int blue = (color >>> 16) & 0xFF;
        int green = Math.round(((color >>> 8) & 0xFF) * 0.8f);
        int red = Math.round((color & 0xFF) * 0.5f);
        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }
}