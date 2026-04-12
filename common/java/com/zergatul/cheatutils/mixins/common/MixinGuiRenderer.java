package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.extensions.GuiRenderStateExtension;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class MixinGuiRenderer {

    @Shadow
    @Final
    private GuiRenderState renderState;

    @Inject(method = "prepareText", at = @At("TAIL"))
    private void onPrepareText(CallbackInfo info) {
        ((GuiRenderStateExtension) this.renderState).forEachCustomText_CU(text -> {
            ((GuiRenderStateExtension) this.renderState).addCustomTextToCurrentLayer_CU(text.asPrepared());
        });
    }
}