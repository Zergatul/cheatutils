package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

import static com.zergatul.cheatutils.render.GlHelper.*;

public class MainFrameBuffer {

    private static int lastFrameBufferObject;

    public static void enter() {
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        GpuTexture colorTexture = target.getColorTexture();
        GpuTexture depthTexture = target.getDepthTexture();
        lastFrameBufferObject = getGlTexture(colorTexture).getFbo(getGlDevice(RenderSystem.getDevice()).directStateAccess(), getGlTexture(depthTexture));
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFrameBufferObject);
    }

    public static void exit() {
        // TODO: the same frame buffer restored???
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFrameBufferObject);
    }
}