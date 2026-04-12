package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jspecify.annotations.Nullable;

public class CustomTextRenderState implements GuiElementRenderState {



    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {

    }

    @Override
    public RenderPipeline pipeline() {
        return null;
    }

    @Override
    public TextureSetup textureSetup() {
        return null;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return null;
    }
}