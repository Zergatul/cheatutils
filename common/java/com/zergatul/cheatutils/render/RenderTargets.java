package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.WindowRenderState;

public class RenderTargets {

    public static RenderTarget getEsp() {
        return EspRenderTargetHolder.INSTANCE;
    }

    private static RenderTarget createEspRenderTarget() {
        WindowRenderState windowState = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState;
        TextureTarget result = new TextureTarget("[" + Constants.MOD_ID + "] ESP", windowState.width, windowState.height, true);
        Events.FramebuffersResize.add(event -> result.resize(event.width(), event.height()));
        return result;
    }

    private static class EspRenderTargetHolder {
        public static final RenderTarget INSTANCE = createEspRenderTarget();
    }
}