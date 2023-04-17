package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GlStates {

    public static void setupOverlayRenderState(boolean blend, boolean depthTest) {
        setupOverlayRenderState(blend, depthTest, Gui.GUI_ICONS_LOCATION);
    }

    public static void setupOverlayRenderState(boolean blend, boolean depthTest, @Nullable ResourceLocation texture) {
        if (blend) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        } else {
            RenderSystem.disableBlend();
        }

        if (depthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }

        if (texture != null)
        {
            RenderSystem.enableTexture();
            RenderSystem.setShaderTexture(0, texture);
        }
        else
        {
            RenderSystem.disableTexture();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }
}