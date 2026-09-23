package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ScaledItemRenderer extends PictureInPictureRenderer<ScaledItemRenderState> {

    private boolean usedOnThisFrame;
    private boolean rendered;

    public ScaledItemRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    public boolean consumeUsedOnThisFrame() {
        boolean used = usedOnThisFrame;
        usedOnThisFrame = false;
        return used;
    }

    @Override
    public Class<ScaledItemRenderState> getRenderStateClass() {
        return ScaledItemRenderState.class;
    }

    @Override
    protected void renderToTexture(ScaledItemRenderState state, PoseStack poseStack) {
        // Same model transform and lighting as vanilla's OversizedItemRenderer.
        poseStack.scale(1, -1, -1);
        ScreenRectangle bounds = state.textureBounds();
        float centerX = (bounds.left() + bounds.right()) / 2f;
        float centerY = (bounds.top() + bounds.bottom()) / 2f;
        poseStack.translate((state.item().x() + 8 - centerX) / 16f, (centerY - state.item().y() - 8) / 16f, 0);

        TrackingItemStackRenderState item = state.item().itemStackRenderState();
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(item.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT);
        FeatureRenderDispatcher dispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
        item.submit(poseStack, dispatcher.getSubmitNodeStorage(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        dispatcher.renderAllFeatures();
        rendered = true;
    }

    @Override
    protected boolean textureIsReadyToBlit(ScaledItemRenderState state) {
        // Each renderer belongs to one model identity and pixel scale.
        return rendered && !state.item().itemStackRenderState().isAnimated();
    }

    @Override
    protected void blitTexture(ScaledItemRenderState state, GuiRenderState guiRenderState) {
        super.blitTexture(state, guiRenderState);
        usedOnThisFrame = true;
    }

    @Override
    protected float getTranslateY(int height, int scale) {
        return height / 2f;
    }

    @Override
    protected String getTextureLabel() {
        return "cheatutils_scaled_item";
    }
}