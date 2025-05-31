package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.font.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen {

    @Unique
    private CompletableFuture<GlyphRenderer> rendererFuture;

    @Unique
    private GlyphFontRenderer renderer;

    @Unique
    private String debugText = "Hello World!!!";

    @Inject(at = @At("TAIL"), method = "render")
    private void onAfterRender(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        /*if (rendererFuture == null) {
            rendererFuture = FontLibrary2.instance.createRenderer(new FontParameters(FontRendererType.AWT, "Consolas", 40, true));
        }
        if (renderer == null) {
            if (rendererFuture.isDone()) {
                renderer = rendererFuture.join().createFontRenderer(new FontRenderDetails(0));
            }
        }

        if (renderer != null) {
            Minecraft mc = Minecraft.getInstance();
            int scrWidth = mc.getWindow().getWidth();
            int scrHeight = mc.getWindow().getHeight();
            int halfScrWidth = scrWidth / 2;
            int halfScrHeight = scrHeight / 2;

            Matrix4f matrix = new Matrix4f();
            matrix.ortho(-halfScrWidth, scrWidth - halfScrWidth, scrHeight - halfScrHeight, -halfScrHeight, -1, 1);

            GlStateTracker.save(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
            MainFrameBuffer.enter();
            renderer.drawText(matrix, "Hello World!!! or no???", -halfScrWidth + 50, -halfScrHeight + 50, Color.WHITE.getRGB());

            TextureColor2dRenderer r = RenderUtilities.instance.getTextureColor2dRenderer();
            r.begin();
            r.rect(
                    -halfScrWidth + 50, 0,
                    renderer.getTexture().texture.getWidth(), renderer.getTexture().texture.getHeight(),
                    1, 1, 1, 1);
            r.end(matrix, renderer.getTexture().getId());

            GlStateTracker.restore(GlStateTracker.PROGRAM | GlStateTracker.TEXTURE);
        }*/
    }
}