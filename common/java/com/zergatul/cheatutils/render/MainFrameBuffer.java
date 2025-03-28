package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL30;

public class MainFrameBuffer {

    private static int lastFrameBufferObject;

    public static void enter() {
        RenderTarget target = RenderType.solid().getRenderTarget();
        GpuTexture colorTexture = target.getColorTexture();
        GpuTexture depthTexture = target.getDepthTexture();
        lastFrameBufferObject = ((GlTexture) colorTexture).getFbo(((GlDevice) RenderSystem.getDevice()).directStateAccess(), depthTexture);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFrameBufferObject);
    }

    public static void exit() {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFrameBufferObject);
    }
}