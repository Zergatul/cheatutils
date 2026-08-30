package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.schematics.SodiumSchematicaRendering;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class MixinBlockRenderer {

    @Inject(
            method = "processQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;colorizeQuad(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;I)V",
                    shift = At.Shift.AFTER),
            remap = false)
    private void onAfterColorizeQuad(MutableQuadViewImpl quad, CallbackInfo info) {
        if (!SodiumSchematicaRendering.isShaded()) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            quad.color(i, this.shadeColor_CU(quad.color(i)));
        }
    }

    @Unique
    private int shadeColor_CU(int color) {
        int alpha = Math.round(((color >>> 24) & 0xFF) * 0.6f);
        int red = Math.round(((color >>> 16) & 0xFF) * 0.5f);
        int green = Math.round(((color >>> 8) & 0xFF) * 0.8f);
        int blue = color & 0xFF;
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}