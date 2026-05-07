package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.GpuFormat;
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

    public static RenderTarget getStatusOverlay() {
        return StatusOverlayRenderTargetHolder.INSTANCE;
    }

    private static RenderTarget createEspRenderTarget() {
        WindowRenderState windowState = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState;
        TextureTarget result = new TextureTarget(
                "[" + Constants.MOD_ID + "] ESP",
                windowState.width,
                windowState.height,
                true,
                GpuFormat.RGBA8_UNORM);
        Events.FramebuffersResize.add(event -> result.resize(event.width(), event.height()));
        return result;
    }

    private static RenderTarget createStatusOverlayRenderTarget() {
        WindowRenderState windowState = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState;
        TextureTarget result = new TextureTarget(
                "[" + Constants.MOD_ID + "] Status Overlay",
                windowState.width,
                windowState.height,
                false,
                GpuFormat.RGBA8_UNORM);
        Events.FramebuffersResize.add(event -> result.resize(event.width(), event.height()));
        return result;
    }

    private static class EspRenderTargetHolder {
        public static final RenderTarget INSTANCE = createEspRenderTarget();
    }

    private static class StatusOverlayRenderTargetHolder {
        public static final RenderTarget INSTANCE = createStatusOverlayRenderTarget();
    }
}