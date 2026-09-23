package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.render.ScaledItemRenderer;
import com.zergatul.cheatutils.render.ScaledItemRenderState;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(GuiRenderer.class)
public abstract class MixinGuiRenderer {

    @Final
    @Shadow
    private MultiBufferSource.BufferSource bufferSource;

    @Final
    @Shadow
    GuiRenderState renderState;

    @Unique
    private final Map<List<Object>, ScaledItemRenderer> scaledItemRenderers_CU = new HashMap<>();

    @Inject(method = "preparePictureInPicture", at = @At("HEAD"))
    private void onPreparePictureInPicture(CallbackInfo info) {
        // NeoForge changes the per-state helper's signature and uses two passes.
        // Prepare our states once here; neither loader has a registered renderer for them.
        renderState.forEachPictureInPicture(state -> {
            if (state instanceof ScaledItemRenderState itemState) {
                // Distinct textures are needed until the deferred GUI draw has consumed every blit.
                List<Object> key = List.of(itemState.item().itemStackRenderState().getModelIdentity(), itemState.itemScale());
                ScaledItemRenderer renderer = scaledItemRenderers_CU.computeIfAbsent(key, ignored -> new ScaledItemRenderer(bufferSource));
                renderer.prepare(itemState, renderState, itemState.itemScale());
            }
        });
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onAfterRender(CallbackInfo info) {
        scaledItemRenderers_CU.values().removeIf(renderer -> {
            if (renderer.consumeUsedOnThisFrame()) {
                return false;
            }
            renderer.close();
            return true;
        });
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void onClose(CallbackInfo info) {
        scaledItemRenderers_CU.values().forEach(ScaledItemRenderer::close);
        scaledItemRenderers_CU.clear();
    }
}